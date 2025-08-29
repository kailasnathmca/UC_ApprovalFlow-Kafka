pipeline {
  agent any
  options { skipDefaultCheckout(true) }
  environment {
    REGISTRY = 'docker.io/your-docker-id'
    APP_NAME = 'proposal-service'
    IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    KUBE_CONTEXT = 'docker-desktop' // or minikube
  }
  stages {
    stage('Checkout') { steps { checkout scm } }

    stage('Build & Test') {
      steps {
        sh 'mvn -B -DskipTests=false -pl proposal-service -am clean verify'
        junit '**/target/surefire-reports/*.xml'
      }
    }

    stage('Docker Build & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DU', passwordVariable: 'DP')]) {
          sh """
            docker build -t $REGISTRY/$APP_NAME:$IMAGE_TAG proposal-service
            echo $DP | docker login -u $DU --password-stdin
            docker push $REGISTRY/$APP_NAME:$IMAGE_TAG
          """
        }
      }
    }

    stage('Deploy (Helm)') {
      when { expression { return params.DIRECT_HELM_DEPLOY == true } }
      steps {
        sh """
          kubectl config use-context $KUBE_CONTEXT
          kubectl get ns uc-ipm || kubectl create ns uc-ipm
          helm upgrade --install $APP_NAME charts/proposal-service \
            --namespace uc-ipm \
            --set image.repository=$REGISTRY/$APP_NAME \
            --set image.tag=$IMAGE_TAG
        """
      }
    }
  }
  parameters {
    booleanParam(name: 'DIRECT_HELM_DEPLOY', defaultValue: true, description: 'Deploy using Helm from Jenkins')
  }
}
