def call (String dockerRegistry, String dockerImageTag, String kubernetesDeployment, String kubernetesContainer, String awsCredID, String awsRegion, String eksClusterName, String kubernetesNamespace = 'default') {
    sh """
        if ! command -v aws > /dev/null; then
            echo "AWS CLI not found. Installing AWS CLI..."
            curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" 2> /dev/null
            
            dpkg-query -l unzip 1>/dev/null || true
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
        if ! command -v kubectl > /dev/null; then
            curl -LO "https://dl.k8s.io/release/\$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
            sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
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

                kubectl get deploy -n $kubernetesNamespace $kubernetesDeployment || true; if [ \$? -ne 0 ]; then
                    echo "Updating image of deployment $kubernetesDeployment in namespace $kubernetesNamespace"
                    kubectl set image deploy -n $kubernetesNamespace $kubernetesDeployment $kubernetesContainer="$dockerRegistry:$dockerImageTag" --record
                else
                    echo "Creating deployment $kubernetesDeployment in namespace $kubernetesNamespace from manifest file"
                    kubectl apply -n $kubernetesNamespace -f manifest.yml --record
                    kubectl set image deploy -n $kubernetesNamespace $kubernetesDeployment $kubernetesContainer="$dockerRegistry:$dockerImageTag" --record
                fi
            """
        }
}
