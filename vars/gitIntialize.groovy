def call(String gitCredID) {
    checkout([$class: 'GitSCM', branches: [[name: "master"]], extensions: [], userRemoteConfigs: [[credentialsId: gitCredID, url: "${env.REPO_URL}"]]])

    sh """
        #Create branches: master_staging, development
        git branch master_staging
        git branch development

        # Checkout feature/initial_commit branch and clone the external repo
        git checkout -b feature/initial_commit
        git clone -b master --single-branch https://github.com/artisantek/code-framework.git cloned_repo

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
