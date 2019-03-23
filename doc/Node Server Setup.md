## Setup
1. Open port `8080`
2. Clone the repo
3. In the `node_app/` folder, execute the following commands:
```
sudo npm install forever -g
npm install
```

## Starting The Server
`npm start`

## Stopping The Server
`npm stop`

## Start The Server (Dev)
`npm run-script dev`  
This will start the server using the nodemon package. Everything will be printed to stdout and the server will automatically restart when changes on the filesystem are made.
