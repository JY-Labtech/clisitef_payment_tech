#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
NATIVE_MODULE="${PROJECT_ROOT}/android/native"
GRADLEW="${PROJECT_ROOT}/example/android/gradlew"
OUTPUT_DIR="${NATIVE_MODULE}/build/outputs/aar"
TARGET_LIBS_DIR="${PROJECT_ROOT}/android/libs"
TARGET_AAR="${TARGET_LIBS_DIR}/clisitef_payment_tech-release.aar"
TARGET_JAR="${TARGET_LIBS_DIR}/clisitef_payment_tech-native.jar"

# Detecta FLUTTER_SDK automaticamente, se necessário
if [[ -z "${FLUTTER_SDK:-}" ]]; then
  if command -v flutter >/dev/null 2>&1; then
    FLUTTER_BIN="$(command -v flutter)"
    FLUTTER_SDK="$(cd "$(dirname "${FLUTTER_BIN}")/.." && pwd)"
    export FLUTTER_SDK
    echo "[INFO] FLUTTER_SDK detectado automaticamente em ${FLUTTER_SDK}."
  else
    echo "[ERRO] Não foi possível localizar o Flutter no PATH nem a variável FLUTTER_SDK. Configure um dos dois." >&2
    exit 1
  fi
else
  export FLUTTER_SDK
  echo "[INFO] FLUTTER_SDK definido manualmente em ${FLUTTER_SDK}."
fi

# Detecta JAVA_HOME automaticamente, se necessário
if [[ -z "${JAVA_HOME:-}" || ! -d "${JAVA_HOME}" ]]; then
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    if JAVA_DETECTED=$(/usr/libexec/java_home -v 17 2>/dev/null); then
      JAVA_HOME="${JAVA_DETECTED}"
    elif JAVA_DETECTED=$(/usr/libexec/java_home 2>/dev/null); then
      JAVA_HOME="${JAVA_DETECTED}"
    else
      echo "[ERRO] Não foi possível detectar automaticamente o JAVA_HOME. Instale o OpenJDK (ex: brew install openjdk@17)." >&2
      exit 1
    fi
    export JAVA_HOME
    echo "[INFO] JAVA_HOME detectado automaticamente em ${JAVA_HOME}."
  else
    echo "[ERRO] JAVA_HOME não está configurado e /usr/libexec/java_home não está disponível." >&2
    exit 1
  fi
else
  export JAVA_HOME
  echo "[INFO] JAVA_HOME definido manualmente em ${JAVA_HOME}."
fi

if [[ ! -x "${GRADLEW}" ]]; then
  echo "[ERRO] Wrapper Gradle não encontrado em ${GRADLEW}. Certifique-se de que o projeto de exemplo foi gerado corretamente." >&2
  exit 1
fi

LOCAL_PROPERTIES="${NATIVE_MODULE}/local.properties"
if [[ ! -f "${LOCAL_PROPERTIES}" ]]; then
  if [[ -f "${PROJECT_ROOT}/android/local.properties" ]]; then
    cp "${PROJECT_ROOT}/android/local.properties" "${LOCAL_PROPERTIES}"
    echo "[INFO] local.properties copiado do módulo android raiz."
  elif [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
    printf "sdk.dir=%s\n" "${ANDROID_SDK_ROOT}" > "${LOCAL_PROPERTIES}"
    echo "[INFO] local.properties gerado a partir de ANDROID_SDK_ROOT."
  elif [[ -n "${ANDROID_HOME:-}" ]]; then
    printf "sdk.dir=%s\n" "${ANDROID_HOME}" > "${LOCAL_PROPERTIES}"
    echo "[INFO] local.properties gerado a partir de ANDROID_HOME."
  else
    echo "[ERRO] Não foi possível determinar o sdk.dir. Configure ANDROID_SDK_ROOT ou forneça android/local.properties." >&2
    exit 1
  fi
fi

# Garante que o sdk.dir aponte para um diretório válido
SDK_DIR=$(grep "^sdk.dir=" "${LOCAL_PROPERTIES}" | cut -d'=' -f2- || true)
if [[ -n "${SDK_DIR}" && ! -d "${SDK_DIR}" ]]; then
  if [[ -n "${ANDROID_SDK_ROOT:-}" && -d "${ANDROID_SDK_ROOT}" ]]; then
    printf "sdk.dir=%s\n" "${ANDROID_SDK_ROOT}" > "${LOCAL_PROPERTIES}"
    echo "[INFO] sdk.dir ajustado para ${ANDROID_SDK_ROOT}."
  elif [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}" ]]; then
    printf "sdk.dir=%s\n" "${ANDROID_HOME}" > "${LOCAL_PROPERTIES}"
    echo "[INFO] sdk.dir ajustado para ${ANDROID_HOME}."
  elif [[ -d "${HOME}/Library/Android/sdk" ]]; then
    printf "sdk.dir=%s\n" "${HOME}/Library/Android/sdk" > "${LOCAL_PROPERTIES}"
    echo "[INFO] sdk.dir ajustado automaticamente para ${HOME}/Library/Android/sdk."
  else
    echo "[ERRO] sdk.dir em ${LOCAL_PROPERTIES} é inválido e não foi possível detectar o Android SDK automaticamente." >&2
    exit 1
  fi
fi

FLUTTER_BIN_PATH="${FLUTTER_SDK}/bin/flutter"
if [[ -x "${FLUTTER_BIN_PATH}" ]]; then
  echo "[INFO] Garantindo artefatos Android do Flutter (precache)..."
  "${FLUTTER_BIN_PATH}" --disable-analytics precache --android >/dev/null 2>&1 || true
fi

echo "[INFO] Limpando e compilando o módulo nativo..."
FLUTTER_SDK="${FLUTTER_SDK}" JAVA_HOME="${JAVA_HOME}" "${GRADLEW}" -p "${NATIVE_MODULE}" --no-daemon clean assembleRelease

if [[ ! -d "${OUTPUT_DIR}" ]]; then
  echo "[ERRO] Diretório de saída ${OUTPUT_DIR} não encontrado." >&2
  exit 1
fi

AAR_FILE=$(find "${OUTPUT_DIR}" -maxdepth 1 -name '*-release.aar' -print -quit)
if [[ -z "${AAR_FILE}" || ! -f "${AAR_FILE}" ]]; then
  echo "[ERRO] AAR release não encontrado em ${OUTPUT_DIR}. Verifique os logs do Gradle." >&2
  exit 1
fi

mkdir -p "${TARGET_LIBS_DIR}"
cp "${AAR_FILE}" "${TARGET_AAR}"

echo "[INFO] Extraindo classes.jar para ${TARGET_JAR}..."
unzip -p "${AAR_FILE}" classes.jar > "${TARGET_JAR}"

# Atualiza JARs auxiliares extraídos de AARs proprietários
while IFS='|' read -r aar_file jar_file; do
  [[ -z "${aar_file}" ]] && continue
  src="${TARGET_LIBS_DIR}/${aar_file}"
  dst="${TARGET_LIBS_DIR}/${jar_file}"
  if [[ -f "${src}" ]]; then
    if [[ ! -f "${dst}" || "${src}" -nt "${dst}" ]]; then
      echo "[INFO] Atualizando ${dst} a partir de ${aar_file}..."
      unzip -p "${src}" classes.jar > "${dst}"
    fi
  else
    echo "[AVISO] AAR ${src} não encontrado; pulando extração do classes.jar associado."
  fi
done <<'EOF'
libgedi-1.16.8-gpos700-payment-release.aar|libgedi-1.16.8-gpos700-payment-release.jar
ppcomp_release-1.29.8.200623.aar|ppcomp_release-1.29.8.200623.jar
EOF

echo "[INFO] AAR copiado para ${TARGET_AAR}."
echo "[INFO] Jar nativo copiado para ${TARGET_JAR}."
echo "[INFO] Processo concluído com sucesso."


