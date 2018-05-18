@Grab('org.yaml:snakeyaml:1.21')
import org.yaml.snakeyaml.Yaml

def call(String yamlPath) {
  Yaml yaml = new Yaml()
  Map pipeline = yaml.load((yamlPath as File).text)
  
  pipeline.stages.each { s ->
    try {
      stage("$s.name") {
	parallel(
	  s.jobs.each { j ->
	    "$j": build(job: "$j")
	  }
	)
      }
    } catch(e) {
      currentBuild.result = 'FAILURE'
    }
  }
}
