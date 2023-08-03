def call (String dockerRegistry, String dockerImageTag, String kubernetesDeployment, String kubernetesContainer, String awsCredID, String awsRegion, String eksClusterName) {
    sh """
        if ! command -v aws > /dev/null; then
            echo "AWS CLI not found. Installing AWS CLI..."
            curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" 2> /dev/null
            
            dpkg-query -l unzip 1>/dev/null
            if [ \$? -ne 0 ]; then
                echo "Unzip is not installed. Installing unzip..."
                sudo apt update &> /dev/null
                sudo apt -y install unzip &> /dev/null
                echo "Unzip installed successfully."
            fi
            
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
                aws configure set aws_access_key_id $awsAccessKey
                aws configure set aws_secret_access_key $awsSecretKey
                aws configure set region $awsRegion

                aws eks --region $awsRegion update-kubeconfig --name $eksClusterName

                kubectl get deploy $kubernetesDeployment || true; if [ \$? -ne 0 ]; then
                    echo "Updating image of deployment $kubernetesDeployment"
                    kubectl set image deploy $kubernetesDeployment $kubernetesContainer="$dockerRegistry:$dockerImageTag" --record
                else
                    echo "Creating deployment $kubernetesDeployment from manifest file"
                    kubectl apply -f manifest.yml --record
                    kubectl set image deploy $kubernetesDeployment $kubernetesContainer="$dockerRegistry:$dockerImageTag" --record
                fi
            """
        }
}
