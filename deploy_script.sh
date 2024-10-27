#!/bin/bash

PEM_FILE=$1  # Se pasa la ruta del archivo .pem como parámetro

# Mostrar el valor de PEM_FILE
echo "Ruta del archivo PEM: $PEM_FILE"

# Cambia esto por el ID de tu instancia
INSTANCE_ID="i-0522ac1d2f06823ea"
PUBLIC_IP=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --query "Reservations[*].Instances[*].PublicIpAddress" --output text)

# Comprobar si se obtuvo la IP pública
if [ -z "$PUBLIC_IP" ]; then
  echo "No se pudo encontrar la IP pública de la instancia."
  exit 1
fi

# Verificar la versión de Java en EC2
echo "Verificando versiones en EC2"
ssh -i "$PEM_FILE" ec2-user@$PUBLIC_IP "java -version"
ssh -i "$PEM_FILE" ec2-user@$PUBLIC_IP "mvn -v"

# Mostrar el directorio actual en tu máquina local
echo "Directorio actual: $(pwd)"

# Compilar el proyecto con Maven y crear el JAR
echo "Compilando el proyecto con Maven..."
mvn clean package || { echo "Error en la compilación del proyecto"; exit 1; }

# Crear el directorio .ssh si no existe
mkdir -p ~/.ssh

# Establecer permisos correctos para el directorio .ssh
chmod 700 ~/.ssh

# Agregar la clave del host a known_hosts
ssh-keyscan -H $PUBLIC_IP >> ~/.ssh/known_hosts

# Establecer permisos correctos para el archivo known_hosts
chmod 644 ~/.ssh/known_hosts

# Asegurarse de que el directorio de la aplicación existe
ssh -i "$PEM_FILE" ec2-user@$PUBLIC_IP << EOF
  echo "Asegurando que el directorio /home/ec2-user/shopsqs existe..."
  mkdir -p /home/ec2-user/shopsqs
EOF

# Copiar el nuevo JAR desde tu máquina local a la instancia EC2
echo "Copiando el archivo JAR a la instancia EC2..."
scp -i "$PEM_FILE" target/demo-0.0.1-SNAPSHOT.jar ec2-user@$PUBLIC_IP:/home/ec2-user/shopsqs/ || { echo "Error al copiar el archivo JAR"; exit 1; }

# Conectarse a la instancia EC2 y realizar las operaciones en remoto
echo "Conectándose a la instancia EC2 para crear el servicio y realizar el despliegue..."
ssh -i "$PEM_FILE" ec2-user@$PUBLIC_IP << EOF
  echo "Navegando al directorio /home/ec2-user/shopsqs"
  cd /home/ec2-user/shopsqs || { echo "No se pudo acceder al directorio"; exit 1; }

  # Crear el archivo de servicio
  echo "Creando el archivo de servicio shopsqs.service..."
  cat <<EOL | sudo tee /etc/systemd/system/shopsqs.service
[Unit]
Description=Mi Servicio ShopsQS
After=network.target

[Service]
ExecStart=/usr/bin/java -jar /home/ec2-user/shopsqs/demo-0.0.1-SNAPSHOT.jar
User=ec2-user
Restart=always

[Install]
WantedBy=multi-user.target
EOL

  # Recargar los servicios y habilitar el nuevo servicio
  sudo systemctl daemon-reload
  sudo systemctl enable shopsqs.service

  # Iniciar el servicio
  echo "Iniciando el servicio..."
  sudo systemctl start shopsqs.service || { echo "Error al iniciar el servicio"; exit 1; }

  echo "Despliegue completado."
EOF
