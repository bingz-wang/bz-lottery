pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '20'))
    disableConcurrentBuilds()
    skipDefaultCheckout(false)
    timestamps()
  }

  parameters {
    choice(name: 'PIPELINE_MODE', choices: ['quick', 'full'], description: 'quick skips the smoke test, full runs everything')
    booleanParam(name: 'SKIP_BACKEND', defaultValue: false, description: 'Skip Maven backend stages')
    booleanParam(name: 'SKIP_FRONTEND', defaultValue: false, description: 'Skip Node frontend stages')
    booleanParam(name: 'RUN_SMOKE_TEST', defaultValue: false, description: 'Run smoke checks against already running local services')
    string(name: 'BACKEND_MAVEN_GOALS', defaultValue: 'clean verify', description: 'Maven goals for the backend reactor')
    string(name: 'SMOKE_BASE_URL', defaultValue: 'http://localhost:9008', description: 'Gateway base URL for smoke checks')
    string(name: 'SMOKE_FRONTEND_URL', defaultValue: 'http://localhost:9010', description: 'Frontend or nginx entry URL for smoke checks')
  }

  environment {
    BACKEND_DIR = 'lottery-platform-backend'
    FRONTEND_DIR = 'lottery-platform-frontend'
    CI = 'true'
  }

  stages {
    stage('Workspace') {
      steps {
        deleteDir()
        checkout scm
      }
    }

    stage('Toolchain Check') {
      steps {
        sh 'java -version'
        sh 'mvn -version'
        sh 'node -v'
        sh 'npm -v'
      }
    }

    stage('Backend Verify') {
      when {
        expression { !params.SKIP_BACKEND }
      }
      steps {
        sh "chmod +x scripts/ci/backend-verify.sh && scripts/ci/backend-verify.sh '${params.BACKEND_MAVEN_GOALS}'"
      }
    }

    stage('Frontend Install') {
      when {
        expression { !params.SKIP_FRONTEND }
      }
      steps {
        sh 'chmod +x scripts/ci/frontend-build.sh && scripts/ci/frontend-build.sh'
      }
    }

    stage('Smoke Test') {
      when {
        expression { params.RUN_SMOKE_TEST || params.PIPELINE_MODE == 'full' }
      }
      steps {
        sh "chmod +x scripts/ci/smoke-check.sh && scripts/ci/smoke-check.sh '${params.SMOKE_BASE_URL}' '${params.SMOKE_FRONTEND_URL}'"
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'lottery-platform-backend/**/target/surefire-reports/*.xml,lottery-platform-backend/**/target/failsafe-reports/*.xml'
      archiveArtifacts allowEmptyArchive: true, artifacts: 'lottery-platform-backend/**/target/*.jar,lottery-platform-frontend/dist/**'
      cleanWs deleteDirs: true, disableDeferredWipeout: true
    }
  }
}
