def call(String dockerRegistry, String dockerImageTag, String helmChartName, String kubernetesNamespace = 'default') {
    // Determine which values file to use based on the namespace
    def valuesFile = "PROD.yaml" // Default
    if (kubernetesNamespace == "dev") {
        valuesFile = "DEV.yaml"
    } else if (kubernetesNamespace == "staging") {
        valuesFile = "STAGING.yaml"
    }

    // Deploy using Helm
    sh "helm upgrade --install ${helmChartName} helm/ --namespace ${kubernetesNamespace} --create-namespace -f helm/values/${valuesFile}"
}
