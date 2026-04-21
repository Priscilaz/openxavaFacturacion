//Estrategia 2: Modularidad 
def runBuild() {
    echo "Iniciando compilacion del proyecto"
    bat 'mvn clean compile'
    echo "Compilacion finalizada"
}

def runTests() {
    echo "Ejecutando pruebas unitarias"
    bat 'mvn test'
    echo "Pruebas finalizadas"
}

def runQuality() {
    echo "Ejecutando analisis de calidad de codigo"
    bat 'mvn checkstyle:check || exit 0'
    echo "Analisis de calidad finalizado"
}

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
