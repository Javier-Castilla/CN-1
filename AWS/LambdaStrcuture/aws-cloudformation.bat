@echo off
REM ================================================
REM Script: create_stack.bat
REM Crea el stack usando params.json generado antes
REM ================================================

SET /P STACK_NAME=Ingrese el nombre de la pila:
SET TEMPLATE_FILE=main.yml
SET PARAMS_FILE=params.json

FOR /F "tokens=*" %%A IN ('aws sts get-caller-identity --query "Account" --output text') DO SET ACCOUNT_ID=%%A
SET ROLE_ARN=arn:aws:iam::%ACCOUNT_ID%:role/LabRole

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
    echo aws cloudformation describe-stacks --stack-name %STACK_NAME%
) ELSE (
    echo ERROR: Fallo la creacion del stack
)

pause
