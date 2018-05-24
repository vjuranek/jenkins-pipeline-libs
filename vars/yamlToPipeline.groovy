@Grab('org.yaml:snakeyaml:1.21')
import org.yaml.snakeyaml.Yaml

def loadStages(String yamlPath) {
  Yaml yaml = new Yaml()
  yaml.load((yamlPath as File).text)
}

def jobClosures(List jobs) {
  def clos = [:]
  jobs.each {
    def j = it
    clos["$j"] = { -> build(job: "$j")}
  }
  clos
}

def call(String yamlPath) {

  p = loadStages(yamlPath)
  p.stages.each { s ->
    try {
      stage("$s.name") {
	parallel(jobClosures(s.jobs))
      }
    } catch(e) {
      currentBuild.result = 'FAILURE'
    }
  }

}
