FROM eclipse-temurin:17-jdk-jammy

ENV ANDROID_SDK_ROOT=/sdk
ENV PATH=$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH

RUN apt-get update && apt-get install -y wget unzip

# Télécharger et configurer les Android cmdline-tools
RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools \
    && cd $ANDROID_SDK_ROOT/cmdline-tools \
    && wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O cmdline-tools.zip \
    && unzip cmdline-tools.zip \
    && rm cmdline-tools.zip \
    && mv cmdline-tools latest

# Accepter les licences Android
RUN yes | sdkmanager --licenses

# Installer le SDK Android nécessaire (API 33 ici)
RUN sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.0"

# Copier ton projet
WORKDIR /app
COPY . .

# Donner les permissions d’exécution au wrapper Gradle
RUN chmod +x ./gradlew

# Commande par défaut : build APK Debug
CMD ["./gradlew", "assembleDebug"]
