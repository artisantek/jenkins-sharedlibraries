def call (String dockerRegistry, String dockerImageTag, String kubernetesDeployment, String kubernetesContainer) {
    sh """
        kubectl get deploy $kubernetesDeployment || true if [ \$? -eq 0 ]; then
            echo "Updating image of deployment $kubernetesDeployment"
            kubectl set image deploy $kubernetesDeployment $kubernetesContainer="$dockerRegistry:$dockerImageTag" --record
        else
            echo "Creating deployment $kubernetesDeployment from manifest file"

            kubectl apply -f manifest.yml --record
            kubectl set image deploy $kubernetesDeployment $kubernetesContainer="$dockerRegistry:$dockerImageTag" --record
        fi
    """
}
