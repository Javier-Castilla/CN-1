#!/bin/bash
set -e  # detiene el script si ocurre un error

# Cargar parámetros desde params.sh si existe
if [[ -f "params.sh" ]]; then
  echo "📄 Cargando parámetros desde params.sh..."
  source params.sh
fi

# Solicitar nombre de la pila de Lambda/ECR
read -p "Ingrese el nombre de la pila de ECR (por defecto ECR-Lambdas): " STACK_NAME__ECR_LAMBDA
STACK_NAME__ECR_LAMBDA=${STACK_NAME__ECR_LAMBDA:-ECR-Lambdas}

TEMPLATE_FILE_ECR="ecr.yml"
TEMPLATE_FILE="main.yml"
PARAMS_FILE="params.json"

# Obtener ID de cuenta AWS
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
ROLE_ARN="arn:aws:iam::${ACCOUNT_ID}:role/LabRole"

echo "============================================="
echo "Creando stack '${STACK_NAME__ECR_LAMBDA}'"
echo "============================================="

aws cloudformation create-stack \
  --stack-name "${STACK_NAME__ECR_LAMBDA}" \
  --template-body "file://${TEMPLATE_FILE_ECR}" \
  --role-arn "${ROLE_ARN}" \
  --capabilities CAPABILITY_NAMED_IAM

echo "Esperando a que la pila '${STACK_NAME__ECR_LAMBDA}' se cree..."
if aws cloudformation wait stack-create-complete --stack-name "${STACK_NAME__ECR_LAMBDA}"; then
  echo "✅ La pila ${STACK_NAME__ECR_LAMBDA} se creó correctamente."
else
  echo "❌ Error: la creación de la pila ${STACK_NAME__ECR_LAMBDA} falló."
  exit 1
fi

# Ejecutar los scripts de subida de Docker (convertidos a .sh)
for service in books customers orders; do
  script_path="../../${service}/upload-docker.sh"
  if [[ -f "${script_path}" ]]; then
    echo "Ejecutando script de subida para ${service}..."
    bash "${script_path}"
  else
    echo "⚠️ No se encontró ${script_path}, omitiendo."
  fi
done

# Solicitar nombre de pila para API Gateway + Lambda + RDS
read -p "Ingrese el nombre de la pila con API Gateway y Lambda + RDS: " STACK_NAME

echo "============================================="
echo "Creando stack '${STACK_NAME}' usando ${PARAMS_FILE}"
echo "============================================="

if aws cloudformation create-stack \
  --stack-name "${STACK_NAME}" \
  --template-body "file://${TEMPLATE_FILE}" \
  --role-arn "${ROLE_ARN}" \
  --parameters "file://${PARAMS_FILE}" \
  --capabilities CAPABILITY_NAMED_IAM; then

  echo "✅ Stack creado exitosamente!"
  echo "Comandos para verificar:"
  echo "aws cloudformation describe-stacks --stack-name ${STACK_NAME__ECR_LAMBDA}"
  echo "aws cloudformation describe-stacks --stack-name ${STACK_NAME}"

else
  echo "❌ ERROR: Falló la creación del stack."
  exit 1
fi
