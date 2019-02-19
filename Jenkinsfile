String buildSlackMessage() {
    def COLOR_MAP = ['SUCCESS': 'good', 'UNSTABLE': 'warning', 'FAILURE': 'danger', 'ABORTED': 'danger']
    def RESULT_MAP = ['SUCCESS': 'Passed', 'UNSTABLE': 'Unstable', 'FAILURE': 'Failed', 'ABORTED': 'Aborted']

    def repoURL =  "${GIT_URL}".replace(".git", "")
    def shortCommit = "${GIT_COMMIT}".substring(0, 7)
    def buildTime = "Build completed in ${currentBuild.durationString}".replace(' and counting', '')
    def timestamp = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone('UTC'))

    return """    
      {
          "username": "Jenkins",
          "icon_url": "https://i.imgur.com/gLAhVPC.png",
          "attachments": [
              {
                  "fallback": "Build #${currentBuild.number} ${RESULT_MAP[currentBuild.currentResult]}",
                  "color": "${COLOR_MAP[currentBuild.currentResult]}",
                  "title": "Build #${currentBuild.number} ${RESULT_MAP[currentBuild.currentResult]}",
                  "title_link": "${RUN_DISPLAY_URL}",
                  "text": "[${JOB_NAME}]",
                  "fields": [
                      {
                          "title": "Commit",
                          "value": "${repoURL}/commit/${GIT_COMMIT}",
                          "short": false
                      }
                  ],
                  "footer": "${buildTime}",
                  "ts": System.currentTimeMillis() 
              }
          ]
      }
    """
}

def isPRMergeBuild() {
    return (env.BRANCH_NAME ==~ /^PR-\d+$/)
}

pipeline {
    agent {
        docker {
            image 'xmartlabs/jenkins-android'
        }
    }

    stages {
        stage('Setup') {
            steps {
                sh 'chmod +x android/gradlew'
                sh 'cd /opt/android-sdk-linux/tools/bin && yes | ./sdkmanager --licenses'
                withCredentials([
                    file(credentialsId: 'local.properties', variable: 'local'),
                    file(credentialsId: 'debug.keystore', variable: 'debug'),
                    file(credentialsId: 'production.keystore', variable: 'production'),
                ]) {
                    sh "cp \$local android/local.properties"
                    sh 'mkdir android/app/keystore'
                    sh "cp \$debug android/app/keystore/debug.keystore.jks"
                    sh "cp \$production android/app/keystore/production.keystore.jks"
                }
            }
        }
        stage('Build') {
            steps {
                sh 'cd android && ./gradlew :app:build --stacktrace'               
            }
        }
        stage('Test') {
            steps {
                sh './android/gradlew :app:test --stacktrace'
            }
            post {
                always {
                    junit 'android/build/test-results/**/*.xml'
                }
            }
        }
    }
    post {
        always {
            withCredentials([string(credentialsId: 'slackWebhook', variable: 'url')]) {
                script {
                    try {
                        httpRequest url: "${url}", httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: buildSlackMessage()
                    } catch (Exception e) {}
                }
            }
        }
    }
}
