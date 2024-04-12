def call(String dockerRegistry, String dockerImageTag, String helmChartName, String awsCredID, String awsRegion, String eksClusterName, String kubernetesNamespace = 'default') {
    // Ensure AWS CLI is installed
    sh """
        if ! command -v aws > /dev/null; then
            echo "AWS CLI not found. Installing AWS CLI..."
            curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
            dpkg-query -l unzip > /dev/null || sudo apt-get update && sudo apt-get -y install unzip
            unzip awscliv2.zip
            sudo ./aws/install
            rm -rf awscliv2.zip aws
            echo "AWS CLI installed successfully."
        fi
    """

    // Using credentials to configure AWS
    withCredentials([usernamePassword(
        credentialsId: awsCredID,
        usernameVariable: "awsAccessKey",
        passwordVariable: "awsSecretKey"
    )]) {
        sh """
            aws configure set aws_access_key_id ${awsAccessKey}
            aws configure set aws_secret_access_key ${awsSecretKey}
            aws configure set region ${awsRegion}
            aws eks --region ${awsRegion} update-kubeconfig --name ${eksClusterName}
            
            if ! command -v helm > /dev/null; then
                echo "Helm not found. Installing Helm..."
                curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
                chmod 700 get_helm.sh
                ./get_helm.sh
                rm -f get_helm.sh
                echo "Helm installed successfully."
            fi
        """
    }

    // Determine which values file to use based on the namespace
    def valuesFile = "PROD.yaml" // Default
    if (kubernetesNamespace == "dev") {
        valuesFile = "DEV.yaml"
    } else if (kubernetesNamespace == "uat") {
        valuesFile = "UAT.yaml"
    }

    // Deploy using Helm
    sh "helm upgrade --install ${helmChartName} helm/ --namespace ${kubernetesNamespace} --create-namespace --set image.repository=${dockerRegistry}:${dockerImageTag} -f helm/values/${valuesFile}"
}
