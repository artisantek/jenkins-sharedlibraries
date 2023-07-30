def call(String dockerRegistry, String dockerImageTag, String dockerHubCredID){
     withCredentials([usernamePassword(
             credentialsId: "$dockerHubCredID",
             usernameVariable: "dockerUser",
             passwordVariable: "dockerPassword"
     )]) {
         sh "docker login -u '$dockerUser' -p '$dockerPassword'"
     }
     sh "docker image push $dockerRegistry:$dockerImageTag"
     sh "docker image push $dockerRegistry:latest"   
}