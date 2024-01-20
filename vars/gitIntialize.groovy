def call(String gitCredID) {
    checkout([$class: 'GitSCM', branches: [[name: "master"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredID", url: "${env.REPO_URL}"]]])
    
    sh """
        git fetch --all
        git checkout -b feature/initial_commit origin/master
    """

    def codeFrameworkRepo = 'https://github.com/artisantek/code-framework.git'
    def cloneDir = 'cloned_repo'

    checkout([$class: 'GitSCM', 
              branches: [[name: '*/master']], 
              doGenerateSubmoduleConfigurations: false, 
              extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: cloneDir]], 
              userRemoteConfigs: [[url: codeFrameworkRepo, credentialsId: gitCredID]]])
    sh """
        # Copy the contents from the cloned repo to the current workspace
        cp -r cloned_repo/* .

        # Replace the <REPO_NAME> placeholder in Dockerfile and values.yaml with the actual repository name
        sed -i 's/<REPO_NAME>/'"${env.REPO_NAME}"'/g' Dockerfile
        sed -i 's/<REPO_NAME>/'"${env.REPO_NAME}"'/g' values.yaml

        # Clean up the cloned directory
        rm -rf cloned_repo

        # Commit and push changes to feature/initial_commit branch
        git add .
        git commit -m 'Initial commit from https://github.com/artisantek/code-framework.git'
        git push origin feature/initial_commit
    """
}
