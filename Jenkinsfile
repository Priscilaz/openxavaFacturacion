@Library('my-shared-lib') _

pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    stages {

        //Estrategia 1: Etapas
        ///Etapa de obtención de código
        stage('Checkout') {
            steps {
                echo "Codigo obtenido automaticamente desde la rama actual"
                }
        }

        ///Etapa de compilación
        stage('Build') {
            steps {
                echo "Iniciando etapa de build"
                script {
                    runBuild()
                }
            }
        }

        //Estrategia 4: Paralelismo
        stage('Tests y Calidad en paralelo') {
            
            parallel {
                stage('Tests') {
                    steps {
                        echo "Inicio de pruebas en paralelo"
                        script {
                            runTests()
                        }
                    }
                }

                stage('Code Quality') {
                    steps {
                        echo "Inicio de analisis de calidad en paralelo"
                        script {
                            runQuality()
                        }
                    }
                }

                stage('SAST') {
                    steps {
                        echo "Iniciando análisis SAST con Semgrep"

                        bat '''
                        chcp 65001
                        set PYTHONUTF8=1
                        semgrep scan --config auto --sarif --output semgrep-report.sarif
                        '''
                    }
                }
            }
        }

        ///Etapa de empaquetado
        stage('Package') {
            steps {
                echo "Generando paquete del proyecto"
                bat 'mvn package'
                echo "Paquete generado"
            }
        }

        //Estrategia 3: IC por rama (solo main despliega)
        stage('Deploy (solo main)') {
            when {
                branch 'main'
            }
            steps {
                echo "Deploy ejecutándose en MAIN"
            }
        }
    }
     post {
        success {
            echo "Pipeline ejecutado correctamente"
        }
        failure {
            echo "El pipeline fallo. Revisar errores"
        }
    }
}
