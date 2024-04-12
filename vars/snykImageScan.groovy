def call(String dockerRegistry, String dockerImageTag, String snykCred, String snykOrg) {
    // Use withCredentials to inject the Snyk token into the environment
    withCredentials([string(
        credentialsId: "$snykCred",
        variable: 'snykToken'
    )]) {
        // Execute Snyk scan within a shell script
        sh """
            export SNYK_TOKEN=${snykToken}
            snyk container monitor ${dockerRegistry}:${dockerImageTag} --org=${snykOrg}
        """
    }
}