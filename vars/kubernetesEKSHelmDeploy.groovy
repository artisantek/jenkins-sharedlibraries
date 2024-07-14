def call(String dockerRegistry, String dockerImageTag, String helmChartName, String awsCredID, String awsRegion, String eksClusterName, String kubernetesNamespace = 'default') {
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

    // Use the 'aws' type credentials directly
    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: awsCredID]]) {
        sh """
            # Configure AWS CLI
            aws configure set aws_access_key_id \$AWS_ACCESS_KEY_ID
            aws configure set aws_secret_access_key \$AWS_SECRET_ACCESS_KEY
            aws configure set region $awsRegion
            aws eks --region $awsRegion update-kubeconfig --name $eksClusterName

            if ! command -v helm > /dev/null; then
                echo "Helm not found. Installing Helm..."
                curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
                chmod 700 get_helm.sh
                ./get_helm.sh
                rm -f get_helm.sh
                echo "Helm installed successfully."
            fi
        """
        
        sh """
            helm upgrade --install $helmChartName helm/ --namespace $kubernetesNamespace --create-namespace --set image.account="$dockerRegistry" --set image.tag="$dockerImageTag"
        """
    }
}