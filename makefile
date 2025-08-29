build:
\tmvn -q -DskipTests -pl proposal-service -am clean package
image:
\tdocker build -t your-docker-id/proposal-service:dev proposal-service
helm-up:
\thelm upgrade --install proposal-service charts/proposal-service -n uc-ipm --create-namespace --set image.tag=dev
