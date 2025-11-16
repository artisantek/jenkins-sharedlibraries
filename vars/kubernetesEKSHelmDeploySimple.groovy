def call(String helmChartName, String kubernetesNamespace = 'default') {
    // Deploy using Helm
    sh '''
        export KUBECONFIG="/home/ubuntu/.kube/config"
        helm upgrade --install ${helmChartName} helm/ --namespace ${kubernetesNamespace} --create-namespace
    '''
}
