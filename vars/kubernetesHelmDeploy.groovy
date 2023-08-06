def call (String dockerRegistry, String dockerImageTag, String helmChartName) {
    sh 'helm upgrade --install $helmChartName helm/ --set image.repository="$dockerRegistry:$dockerImageTag" '
}

