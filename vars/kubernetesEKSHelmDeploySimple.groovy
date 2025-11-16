def call(String helmChartName, String kubernetesNamespace = 'default') {
    // Deploy using Helm
    sh """
        helm upgrade --install ${helmChartName} helm/ --namespace ${kubernetesNamespace} --create-namespace
    """
}
