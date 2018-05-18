@Grab('org.yaml:snakeyaml:1.21')
import org.yaml.snakeyaml.Yaml

def call(String yamlPath) {
  Yaml yaml = new Yaml()
  Map pipeline = yaml.load((yamlPath as File).text)
  
  pipeline.stages.each{ s ->
    println s.name
    try {
      stage("$s.name") {
	stage.jobs.each{ j ->
	  print "\t$j\n"
	  build(job: "$j")
	}
      }
    } catch(e) {
      e.printStackTrace()
      currentBuild.result = 'FAILURE'
    }
  }
}
