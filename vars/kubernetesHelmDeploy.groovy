def call (String dockerRegistry, String dockerImageTag, String helmChartName) {
    sh """
        if ! command -v helm > /dev/null; then
            echo "Helm not found. Installing Helm..."
            curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
            chmod 700 get_helm.sh
            ./get_helm.sh
            rm -f get_helm.sh
            echo "Helm installed successfully."
        fi

        helm upgrade --install $helmChartName helm/ --set image.repository=$dockerRegistry --set image.tag=$dockerImageTag
    """
}
