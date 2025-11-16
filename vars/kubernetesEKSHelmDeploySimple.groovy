def call(String helmChartName, String kubernetesNamespace = 'default') {
    // Deploy using Helm
    export KUBECONFIG="/home/ubuntu/.kube/config"
    sh "helm upgrade --install ${helmChartName} helm/ --namespace ${kubernetesNamespace} --create-namespace"
}
