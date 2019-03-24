0. Setup Docker
1. Go to `host/jenkins`
2. Type `docker exec docker_jenkins-master_1 cat /var/jenkins_home/secrets/initialAdminPassword`
3. Use password from Step 2 to gain admin access in Jenkins
4. Install suggested plugins
5. Create your admin user
6. Install HTTP Request plugin and update other plugins
7. Add following secrets:  
    - local.properties
      - Contains key aliases and passwords for keystores. Example:
      ```properties
      debug.key.alias=debugkey
      debug.key.password=password
      release.key.alias=prodkey
      release.key.password=password
      ```
    - debug.keystore
      - Keystore file for debug signing
    - production.keystore
      - Keystore file for producting signing
    - slackWebhook
      - Secret Text: URL for incoming webhook in slack
8. Install the following plugins
  - `android-emulator`
9. Open Blue Ocean and add first pipeline
