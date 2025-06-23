def call(String dockerRegistry, String dockerImageTag, String awsRegion){
    // Use the 'aws' type credentials directly
    sh """
        set -e
        
        ECR_REPO=\$(echo "$dockerRegistry" | sed 's|.*amazonaws.com/||' | cut -d'/' -f1 | cut -d':' -f1)
        echo "Extracted ECR repository: \$ECR_REPO"
        
        # Check if the repository exists and create it if it does not
        if ! aws ecr describe-repositories --repository-names \$ECR_REPO --region $awsRegion >/dev/null 2>&1; then
            echo "Repository \$ECR_REPO does not exist. Creating repository."
            aws ecr create-repository --repository-name \$ECR_REPO --region $awsRegion
        else
            echo "Repository \$ECR_REPO already exists."
        fi

        # Login to ECR
        aws ecr get-login-password --region $awsRegion | docker login --username AWS --password-stdin $dockerRegistry

        # Push the Docker images
        docker image push $dockerRegistry:$dockerImageTag
        docker image push $dockerRegistry:latest
    """
}