@Library('my-shared-lib') _

pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    stages {

        // Estrategia 1: Pipeline dividido por etapas

        stage('Checkout') {
            steps {
                echo "Código obtenido automáticamente desde la rama actual"
            }
        }

        stage('Build') {
            steps {

                echo "Iniciando etapa de compilación"

                script {
                    runBuild()
                }

            }
        }

        // Estrategia 4: Paralelismo
        stage('Validaciones') {

            parallel {

                stage('Tests') {

                    steps {

                        echo "Ejecutando pruebas automatizadas"

                        script {
                            runTests()
                        }

                    }

                }

                stage('Semgrep (SAST)') {

                    steps {

                        echo "[Seguridad] Ejecutando análisis SAST con Semgrep"

                        script {
                            runSemgrep()
                        }

                    }

                }

            }

        }

        stage('Checkstyle') {

            steps {

                echo "Ejecutando análisis de calidad del código"

                script {
                    runQuality()
                }

            }

        }

        stage('SonarQube (SAST)') {

            steps {

                echo "[Seguridad] Ejecutando análisis SAST con SonarQube"

                script {
                    runSonar()
                }

            }

        }

        stage('Quality Gate') {

            steps {

                echo "Validando Quality Gate de SonarQube"

                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }

                echo "Quality Gate aprobado. Continuando con el pipeline."

            }

        }

        stage('Package') {

            steps {

                echo "Generando paquete del proyecto"

                bat 'mvn package'

                echo "Paquete generado correctamente"

            }

        }

        stage('Start ManageLab') {

            steps {

                echo "Iniciando ManageLab para el análisis DAST"

                script {
                    startManageLab()
                }

            }

        }

        stage('DAST (OWASP ZAP)') {

            steps {

                echo "[Seguridad] Ejecutando análisis DAST con OWASP ZAP"

                script {
                    runDAST()
                }

            }

        }

        stage('Build Docker Image') {

            steps {

                echo "Construyendo imagen Docker del proyecto"

                script {
                    runDockerBuild()
                }

            }

        }

        stage('Docker Push') {

            steps {
        
                echo "Publicando imagen Docker"
        
                script {
                    runDockerPush()
                }
        
            }
        
        }

        stage('GitOps (Argo CD)') {

            when {
                branch 'main'
            }
        
            steps {
        
                echo "Publicando cambios para que Argo CD sincronice el despliegue."
        
                echo "Argo CD detectará automáticamente los cambios en el repositorio y actualizará Kubernetes."
        
            }
        
        }

        stage('Deploy (solo main)') {

            when {
                branch 'main'
            }

            steps {

                echo "Deploy ejecutándose únicamente en la rama MAIN"

            }

        }

    }

    post {

        always {

            /*script {

                stopManageLab()

            }*/

            archiveArtifacts artifacts: '''
semgrep-report.sarif,
zap-report.html,
zap-report.json,
zap-report.md
''', allowEmptyArchive: true, fingerprint: true

        }

        success {

            echo "Pipeline ejecutado correctamente."

        }

        failure {

            echo "El pipeline falló. Revisar los registros para identificar la causa."

        }

    }

}
