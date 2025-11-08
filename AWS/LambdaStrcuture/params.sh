#!/bin/bash
set -e  # Detener el script si ocurre algún error

TEMPLATE_FILE="main.yml"
PARAMS_FILE="./params.json"

# Leer variables con valores por defecto
read -p "Ingrese el identificador de la BD (por defecto bookstore-db-lambdas): " DB_IDENTIFIER
DB_IDENTIFIER=${DB_IDENTIFIER:-bookstore-db-lambdas}

read -p "Ingrese el tipo de BD (por defecto postgresql): " DB_TYPE
DB_TYPE=${DB_TYPE:-postgresql}

read -p "Ingrese el nombre de la BD (por defecto bookstore-lambdas): " DB_NAME
DB_NAME=${DB_NAME:-bookstore}

read -p "Ingrese el usuario de la BD (por defecto postgres): " DB_USER
DB_USER=${DB_USER:-postgres}

# Leer contraseña de manera segura
read -s -p "Ingrese la contraseña de la base de datos: " DB_PASSWORD
echo ""

# Obtener información de red desde AWS
VPC_ID=$(aws ec2 describe-vpcs --query "Vpcs[0].VpcId" --output text)
echo "VPC ID: ${VPC_ID}"

if [[ -z "$VPC_ID" || "$VPC_ID" == "None" ]]; then
  echo "❌ ERROR: No se pudo obtener el VPC ID."
  exit 1
fi

SUBNETS=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=${VPC_ID}" \
  --query "join(',', Subnets[].SubnetId)" --output text)
echo "Subnets: ${SUBNETS}"

if [[ -z "$SUBNETS" || "$SUBNETS" == "None" ]]; then
  echo "❌ ERROR: No se pudieron obtener las subnets."
  exit 1
fi

# Generar archivo JSON
echo "Generando archivo ${PARAMS_FILE}..."

cat > "${PARAMS_FILE}" <<EOF
[
  {"ParameterKey": "SubnetIds", "ParameterValue": "${SUBNETS}"},
  {"ParameterKey": "VpcId", "ParameterValue": "${VPC_ID}"},
  {"ParameterKey": "DBIdentifier", "ParameterValue": "${DB_IDENTIFIER}"},
  {"ParameterKey": "DBType", "ParameterValue": "${DB_TYPE}"},
  {"ParameterKey": "DBName", "ParameterValue": "${DB_NAME}"},
  {"ParameterKey": "DBUser", "ParameterValue": "${DB_USER}"},
  {"ParameterKey": "DBPassword", "ParameterValue": "${DB_PASSWORD}"}
]
EOF

echo "✅ Archivo ${PARAMS_FILE} generado exitosamente."
