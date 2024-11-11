from flask import Flask
import py_eureka_client.eureka_client as eureka_client
app = Flask(__name__)

# Eureka Client 초기화
eureka_client.init(eureka_server="http://localhost:8761/eureka",
                   app_name="face-service",
                   instance_host="localhost",
                   instance_port=5000
                   )

@app.route('/api/v1/faces/test', methods=['GET'])
def test():
    return "test 성공"


if __name__ == '__main__':  
   app.run('0.0.0.0',port=5000,debug=True)