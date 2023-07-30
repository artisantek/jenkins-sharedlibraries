def call (String gitRepo, String gitBranch, String gitCredID) {
  checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredID", url: "$gitRepo"]]])
}