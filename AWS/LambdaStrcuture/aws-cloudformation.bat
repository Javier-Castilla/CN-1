@echo off
call params.bat

SET /P STACK_NAME_LAMBDA=Ingrese el nombre de la pila de ECR:
SET TEMPLATE_FILE_ECR=ecr.yml
SET TEMPLATE_FILE=main.yml
SET PARAMS_FILE=params.json

FOR /F "tokens=*" %%A IN ('aws sts get-caller-identity --query "Account" --output text') DO SET ACCOUNT_ID=%%A
SET ROLE_ARN=arn:aws:iam::%ACCOUNT_ID%:role/LabRole

echo =============================================
echo Creando stack "%STACK_NAME_LAMBDA%"
echo =============================================

aws cloudformation create-stack ^
    --stack-name %STACK_NAME_LAMBDA% ^
    --template-body file://%TEMPLATE_FILE_ECR% ^
    --role-arn %ROLE_ARN% ^
    --capabilities CAPABILITY_NAMED_IAM

echo Esperando a que la pila "%STACK_NAME_LAMBDA%" se cree...
aws cloudformation wait stack-create-complete --stack-name %STACK_NAME_LAMBDA%

IF %ERRORLEVEL% NEQ 0 (
    echo ❌ Error: la creación de la pila %STACK_NAME_LAMBDA% falló.
    exit /b 1
)

echo ✅ La pila %STACK_NAME_LAMBDA% se creó correctamente.

call ../../books/upload-docker.bat
call ../../customers/upload-docker.bat
call ../../orders/upload-docker.bat

SET /P STACK_NAME=Ingrese el nombre de la pila con API Gateway y Lambda + RDS:

echo =============================================
echo Creando stack "%STACK_NAME%" usando %PARAMS_FILE%
echo =============================================

aws cloudformation create-stack ^
    --stack-name %STACK_NAME% ^
    --template-body file://%TEMPLATE_FILE% ^
    --role-arn %ROLE_ARN% ^
    --parameters file://%PARAMS_FILE% ^
    --capabilities CAPABILITY_NAMED_IAM

IF %ERRORLEVEL% EQU 0 (
    echo Stack creado exitosamente!
    echo Comando para verificar:
    echo aws cloudformation describe-stacks --stack-name %STACK_NAME_LAMBDA%
    echo aws cloudformation describe-stacks --stack-name %STACK_NAME%
) ELSE (
    echo ERROR: Fallo la creacion del stack
)

pause
