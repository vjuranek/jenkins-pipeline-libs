@Grab('org.yaml:snakeyaml:1.21')
import org.yaml.snakeyaml.Yaml

def loadStages(String yamlPath) {
  Yaml yaml = new Yaml()
  yaml.load((yamlPath as File).text)
}

def jobParams(Map params) {
  parArr = []
  for (e in params.entrySet()) { //for some reason params.each { ... } doesn't work
    parArr.add([$class: 'StringParameterValue', name: "${e.key}", value: "${e.value}"])
  }
  parArr
}

def jobClosures(List jobs) {
  def clos = [:]
  jobs.each {
    def j = it
    if (j.parameters) {
      jobParams(j.parameters)
      clos["$j.name"] = { -> build(job: "$j.name", parameters: jobParams(j.parameters))}
    } else {
      clos["$j.name"] = { -> build(job: "$j.name")}
    }
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
    } catch (hudson.AbortException e) {
      if (e.message.contains("UNSTABLE")) {
	currentBuild.result = 'UNSTABLE'
      } else if (e.message.contains("ABORTED")) {
	currentBuild.result = 'ABORTED'
      } else {
	currentBuild.result = 'FAILURE'
      }
    } catch (Exception e) {
      currentBuild.result = 'FAILURE'
    }
  }
}
