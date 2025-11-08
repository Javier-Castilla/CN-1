#!/bin/bash
set -e  # Detener el script ante cualquier error
export DOCKER_BUILDKIT=0

# Ir al directorio del script
cd "$(dirname "$0")"

echo "=============================================="
echo "🚀 Iniciando despliegue Docker a AWS ECR"
echo "=============================================="

# Detectar nombre de imagen según la carpeta actual
IMAGE_NAME=$(basename "$(pwd)")

# Obtener región configurada en AWS CLI
REGION=$(aws configure get region)
if [[ -z "$REGION" ]]; then
  echo "⚠️ No se detectó región configurada. Se usará 'us-east-1' por defecto."
  REGION="us-east-1"
fi

# Obtener ID de cuenta AWS
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text)
if [[ -z "$ACCOUNT_ID" || "$ACCOUNT_ID" == "None" ]]; then
  echo "❌ ERROR: No se pudo obtener el ID de cuenta AWS. Verifica tus credenciales (aws configure)."
  exit 1
fi

# Construir URL del repositorio ECR
ECR_URL="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
REPO_NAME="bookstore:${IMAGE_NAME}"

echo ""
echo "=============================================="
echo "📋 Configuración detectada:"
echo "----------------------------------------------"
echo "Cuenta AWS: ${ACCOUNT_ID}"
echo "Región:     ${REGION}"
echo "Imagen:     ${IMAGE_NAME}"
echo "Repositorio: ${ECR_URL}/${REPO_NAME}"
echo "=============================================="
echo ""

# Compilar proyecto Maven (si aplica)
if [[ -f "./mvnw" ]]; then
  echo "🔧 Compilando proyecto Maven..."
  ./mvnw clean package -DskipTests -U -X
else
  echo "⚠️ No se encontró ./mvnw, se omite compilación Maven."
fi

# Login en Amazon ECR
echo ""
echo "🔐 Iniciando sesión en Amazon ECR..."
aws ecr get-login-password --region "${REGION}" | \
docker login --username AWS --password-stdin "${ECR_URL}"

# Crear repositorio si no existe (idempotente)
echo ""
echo "🪣 Verificando repositorio ECR..."
aws ecr describe-repositories --repository-names "bookstore" --region "${REGION}" >/dev/null 2>&1 || \
aws ecr create-repository --repository-name "bookstore" --region "${REGION}"

# Construir imagen
echo ""
echo "🏗️ Construyendo imagen Docker..."
docker build -t "${IMAGE_NAME}" -f Dockerfile-Lambda .

# Etiquetar imagen
echo ""
echo "🏷️ Etiquetando imagen..."
docker tag "${IMAGE_NAME}:latest" "${ECR_URL}/${REPO_NAME}"

# Subir imagen a ECR
echo ""
echo "☁️ Publicando imagen en ECR..."
docker push "${ECR_URL}/${REPO_NAME}"

echo ""
echo "✅ Imagen publicada correctamente:"
echo "   ${ECR_URL}/${REPO_NAME}"
