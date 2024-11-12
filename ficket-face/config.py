import requests

# 초기 설정 로드 함수
def load_config_from_server():
    # Config Server URL 설정
    CONFIG_SERVER_URL = "http://localhost:8888/face-service/local"
    try:
        response = requests.get(CONFIG_SERVER_URL)
        response.raise_for_status()
        config_data = response.json()
        
        # 필요한 설정 정보 추출
        property_sources = config_data.get("propertySources", [])
        config = {"mysql": {}, "encryption": {}, "rabbitmq": {}}

        for source in property_sources:
            source_data = source["source"]
            # MySQL 설정
            if "flask.mysql.url" in source_data:
                config["mysql"]["url"] = source_data["flask.mysql.url"]
            if "flask.mysql.password" in source_data:
                config["mysql"]["password"] = source_data["flask.mysql.password"]
                
            # Encryption Key 설정
            if "encryption.secret_key" in source_data:
                config["encryption"]["secret_key"] = source_data["encryption.secret_key"]

            # RabbitMQ 설정
            if "spring.rabbitmq.host" in source_data:
                config["rabbitmq"]["host"] = source_data["spring.rabbitmq.host"]
            if "spring.rabbitmq.port" in source_data:
                config["rabbitmq"]["port"] = source_data["spring.rabbitmq.port"]
            if "spring.rabbitmq.username" in source_data:
                config["rabbitmq"]["username"] = source_data["spring.rabbitmq.username"]
            if "spring.rabbitmq.password" in source_data:
                config["rabbitmq"]["password"] = source_data["spring.rabbitmq.password"]

        print("Configuration reloaded:", config)
        return config
    except requests.RequestException as e:
        print("Failed to load configuration from Config Server:", e)
        return {}