def call (String dockerRegistry, String dockerImageTag) {
  sh """
    docker image build -t $dockerRegistry .
    docker image tag $dockerRegistry $dockerRegistry:$dockerImageTag
    docker image tag $dockerRegistry $dockerRegistry:latest
    """
}