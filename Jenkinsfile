pipeline {
    agent any

    environment {
        DOCKER_COMPOSE = 'docker-compose'
        MAVEN_HOME = tool 'Maven-3.9.5'
        JAVA_HOME = tool 'JDK-21'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'GitHub\'dan kodlar çekiliyor...'
                checkout scm
                bat 'git rev-parse HEAD > commit_hash.txt'
                bat 'type commit_hash.txt'
            }
        }

        stage('Build') {
            steps {
                echo 'Kodlar build ediliyor...'
                bat """
                    "%MAVEN_HOME%\\bin\\mvn.cmd" clean compile -DskipTests
                """
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Birim testleri çalıştırılıyor...'
                bat """
                    "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=*Test
                """
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo 'Entegrasyon testleri çalıştırılıyor...'
                bat """
                    "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=*IntegrationTest
                """
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Docker image oluşturuluyor...'
                bat 'docker-compose build banking-app'
            }
        }

        stage('Start Containers') {
            steps {
                echo 'Docker container\'lar başlatılıyor...'
                bat '''
                    docker-compose down -v
                    docker-compose up -d
                    timeout /t 30
                '''
            }
        }

        stage('Health Check') {
            steps {
                echo 'Sistem sağlık kontrolü yapılıyor...'
                bat '''
                    for /L %%i in (1,1,30) do (
                        curl -f http://localhost:8080/api/auth/login >nul 2>&1
                        if !errorlevel! equ 0 (
                            echo Sistem hazır!
                            exit /b 0
                        )
                        echo Bekleniyor... (%%i/30)
                        timeout /t 2 >nul
                    )
                    echo Sistem başlatılamadı!
                    exit /b 1
                '''
            }
        }

        stage('Selenium Test 1 - User Registration') {
            steps {
                echo 'Selenium Test 1: Kullanıcı Kaydı çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test1_UserRegistration
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Test 2 - User Login') {
            steps {
                echo 'Selenium Test 2: Kullanıcı Girişi çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test2_UserLogin
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Test 3 - Account Creation') {
            steps {
                echo 'Selenium Test 3: Hesap Oluşturma çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test3_AccountCreation
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Test 4 - Deposit') {
            steps {
                echo 'Selenium Test 4: Para Yatırma çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test4_Deposit
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Test 5 - Withdrawal') {
            steps {
                echo 'Selenium Test 5: Para Çekme çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test5_Withdrawal
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Test 6 - Transfer') {
            steps {
                echo 'Selenium Test 6: Para Transferi çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test6_Transfer
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Test 7 - Balance Inquiry') {
            steps {
                echo 'Selenium Test 7: Bakiye Sorgulama çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test7_BalanceInquiry
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Test 8 - Transaction History') {
            steps {
                echo 'Selenium Test 8: İşlem Geçmişi çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test8_TransactionHistory
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Test 9 - Invalid Login') {
            steps {
                echo 'Selenium Test 9: Geçersiz Giriş çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test9_InvalidLogin
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Test 10 - Logout') {
            steps {
                echo 'Selenium Test 10: Çıkış Yapma çalıştırılıyor...'
                dir('selenium-tests') {
                    bat """
                        "%MAVEN_HOME%\\bin\\mvn.cmd" test -Dtest=Test10_Logout
                    """
                }
            }
            post {
                always {
                    junit 'selenium-tests/target/surefire-reports/*.xml'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline tamamlandı. Container\'lar durduruluyor...'
            script {
                try {
                    bat 'docker-compose down -v'
                } catch (Exception e) {
                    echo 'Docker container durdurulamadı (Docker çalışmıyor olabilir)'
                }
            }
            junit '**/target/surefire-reports/*.xml'
        }
        success {
            echo 'Tüm testler başarılı!'
        }
        failure {
            echo 'Bazı testler başarısız oldu!'
        }
    }
}

