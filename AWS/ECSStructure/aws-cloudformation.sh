#!/bin/bash
set -e  # Detener el script si ocurre algún error

# Cargar parámetros desde params.sh (si existe)
if [[ -f "params.sh" ]]; then
    echo "📄 Cargando parámetros desde params.sh..."
    source params.sh
else
    echo "⚠️ No se encontró params.sh. Se solicitarán los valores manualmente."
fi

# Si no se definió ECR_REPOSITORY en params.sh, preguntar al usuario
if [[ -z "$ECR_REPOSITORY" ]]; then
  read -p "Ingrese el nombre del repositorio ECR (por defecto monolith): " ECR_REPOSITORY
fi
ECR_REPOSITORY=${ECR_REPOSITORY:-monolith}

# Solicitar nombre de la pila de ECR
read -p "Ingrese el nombre de la pila de ECR (por defecto ECR-Monolith): " STACK_NAME_ECR
STACK_NAME_ECR=${STACK_NAME_ECR:-ECR-Monolith}

# Archivos de plantilla
TEMPLATE_FILE_ECR="ecr.yml"
TEMPLATE_FILE="main.yml"
PARAMS_FILE="params.json"

# Obtener ID de la cuenta AWS y rol
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
ROLE_ARN="arn:aws:iam::${ACCOUNT_ID}:role/LabRole"

echo "============================================="
echo "🚀 Creando stack '${STACK_NAME_ECR}'"
echo "Repositorio ECR: ${ECR_REPOSITORY}"
echo "============================================="

# Crear stack de ECR con parámetro
aws cloudformation create-stack \
  --stack-name "${STACK_NAME_ECR}" \
  --template-body "file://${TEMPLATE_FILE_ECR}" \
  --parameters "ParameterKey=ECRName,ParameterValue=${ECR_REPOSITORY}" \
  --role-arn "${ROLE_ARN}" \
  --capabilities CAPABILITY_NAMED_IAM

echo "⏳ Esperando a que la pila '${STACK_NAME_ECR}' se cree..."
if aws cloudformation wait stack-create-complete --stack-name "${STACK_NAME_ECR}"; then
  echo "✅ La pila ${STACK_NAME_ECR} se creó correctamente."
else
  echo "❌ Error: la creación de la pila ${STACK_NAME_ECR} falló."
  exit 1
fi

# Ejecutar script de carga Docker (convertido también a .sh si es necesario)
if [[ -f "../../monolith/upload-docker.sh" ]]; then
  echo "🚢 Subiendo imagen Docker al repositorio ${ECR_REPOSITORY}..."
  bash ../../monolith/upload-docker.sh "${ECR_REPOSITORY}"
else
  echo "⚠️ No se encontró '../../monolith/upload-docker.sh'."
fi

# Crear stack principal (API Gateway + ECS + RDS)
read -p "Ingrese el nombre de la pila con API Gateway y ECS + RDS: " STACK_NAME

echo "============================================="
echo "🚀 Creando stack '${STACK_NAME}' usando ${PARAMS_FILE}"
echo "============================================="

if aws cloudformation create-stack \
  --stack-name "${STACK_NAME}" \
  --template-body "file://${TEMPLATE_FILE}" \
  --role-arn "${ROLE_ARN}" \
  --parameters "file://${PARAMS_FILE}" \
  --capabilities CAPABILITY_NAMED_IAM; then

  echo "✅ Stack creado exitosamente!"
  echo ""
  echo "Comandos para verificar:"
  echo "aws cloudformation describe-stacks --stack-name ${STACK_NAME_ECR}"
  echo "aws cloudformation describe-stacks --stack-name ${STACK_NAME}"

else
  echo "❌ ERROR: Falló la creación del stack."
  exit 1
fi
