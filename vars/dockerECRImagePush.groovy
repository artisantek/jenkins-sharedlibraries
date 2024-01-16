def call(String dockerRegistry, String dockerImageTag, String awsCredID, String awsRegion){
    sh """
        if ! command -v aws > /dev/null; then
            echo "AWS CLI not found. Installing AWS CLI..."
            curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" 2> /dev/null
            
            dpkg-query -l unzip > /dev/null || sudo apt-get update && sudo apt-get -y install unzip
            
            unzip awscliv2.zip > /dev/null
            sudo ./aws/install > /dev/null
            rm -rf awscliv2.zip aws
            echo "AWS CLI installed successfully."
        fi
    """

    withCredentials([usernamePassword(
        credentialsId: "$awsCredID",
        usernameVariable: "awsAccessKey",
        passwordVariable: "awsSecretKey"
    )]) {
        sh """
            # Extract the repository name from the dockerRegistry URL
            repositoryName=\$(echo ${dockerRegistry} | awk -F'/' '{print \$NF}')

            # Configure AWS CLI
            aws configure set aws_access_key_id $awsAccessKey
            aws configure set aws_secret_access_key $awsSecretKey
            aws configure set region $awsRegion

            # Check if the repository exists and create it if it does not
            if ! aws ecr describe-repositories --repository-names $repositoryName --region $awsRegion >/dev/null 2>&1; then
                echo "Repository $repositoryName does not exist. Creating repository."
                aws ecr create-repository --repository-name $repositoryName --region $awsRegion
            else
                echo "Repository $repositoryName already exists."
            fi

            # Login to Docker registry
            aws ecr get-login-password --region $awsRegion | docker login --username AWS --password-stdin $dockerRegistry

            # Push the Docker images
            docker image push $dockerRegistry:$dockerImageTag
            docker image push $dockerRegistry:latest
        """
    }
}
