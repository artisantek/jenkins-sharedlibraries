def call(String dockerRegistry, String dockerImageTag, String dockerHubCredID){
     withCredentials([usernamePassword(
             credentialsId: "$dockerHubCredID",
             usernameVariable: "USER",
             passwordVariable: "PASS"
     )]) {
         sh "docker login -u '$USER' -p '$PASS'"
     }
     sh "docker image push $dockerRegistry:$dockerImageTag"
     sh "docker image push $dockerRegistry:latest"   
}