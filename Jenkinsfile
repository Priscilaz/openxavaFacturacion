@Library('my-shared-lib') _

pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    stages {

        // Estrategia 1: Pipeline dividido por etapas

        // Etapa de obtención de código
        stage('Checkout') {
            steps {
                echo "Código obtenido automáticamente desde la rama actual"
            }
        }

        // Etapa de compilación
        stage('Build') {
            steps {
                echo "Iniciando etapa de compilación"

                script {
                    runBuild()
                }
            }
        }

        // Estrategia 4: Ejecución de validaciones en paralelo
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

                stage('Calidad') {
                    steps {
                        echo "Ejecutando análisis de calidad del código"

                        script {
                            runQuality()
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

                stage('SonarQube (SAST)') {
                    steps {
                        echo "[Seguridad] Ejecutando análisis SAST con SonarQube"

                        script {
                            runSonar()
                        }
                    }
                }
            }
        }

    
        stage('Quality Gate') {
            steps {
        
                echo "Validando Quality Gate de SonarQube..."
        
                timeout(time: 5, unit: 'MINUTES') {
        
                    waitForQualityGate abortPipeline: true
        
                }
        
                echo "Quality Gate aprobado. Continuando con el pipeline."
        
            }
        }
        

        // Etapa de empaquetado
        stage('Package') {
            steps {

                echo "Generando paquete del proyecto"

                bat 'mvn package'

                echo "Paquete generado correctamente"

            }
        }

        // Estrategia 3: Integración Continua por rama
        // Solo la rama principal realiza el despliegue
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

            // Evidencias generadas por las herramientas de seguridad
            archiveArtifacts artifacts: 'semgrep-report.sarif', fingerprint: true

        }

        success {

            echo "Pipeline ejecutado correctamente."

        }

        failure {

            echo "El pipeline falló. Revisar los registros para identificar la causa."

        }
    }
}
