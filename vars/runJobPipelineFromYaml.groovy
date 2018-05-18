@Grab('org.yaml:snakeyaml:1.21')
import org.yaml.snakeyaml.Yaml

def call(String yamlPath) {
  Yaml yaml = new Yaml()
  Map pipeline = yaml.load((yamlPath as File).text)
  
  pipeline.stages.each{ stage ->
    println stage.name
    try {
      stage("$stage.name") {
	stage.jobs.each{ job ->
	  print "\t$job\n"
	  build(job: "$job")
	}
      }
    } catch(e) {
      currentBuild.result = 'FAILURE'
    }
  }
}
