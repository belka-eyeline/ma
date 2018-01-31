#include <stdio.h>

#include <sys/types.h>
#include <signal.h>
#include <string.h>
#include <unistd.h>

#include <jvmti.h>

static void JNICALL
resourceExhausted(
        jvmtiEnv *jvmti_env,
        JNIEnv *jni_env,
        jint flags,
        const void *reserved,
        const char *description);


#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint JNICALL
Agent_OnLoad(
        JavaVM *vm,
        char *options,
        void *reserved) {

    jvmtiEnv *env;
    jvmtiError err;

    jint rc = (*vm)->GetEnv(vm, (void **) &env, JVMTI_VERSION);
    if (rc != JNI_OK) {
        fprintf(stderr, "OOMKiller: GetEnv failed, code = %d\n", rc);
        return JNI_ERR;
    }

    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));

    callbacks.ResourceExhausted = &resourceExhausted;

    err = (*env)->SetEventCallbacks(env, &callbacks, sizeof(callbacks));
    if (err != JVMTI_ERROR_NONE) {
        fprintf(stderr, "OOMKiller: SetEventCallbacks failed, code = %d\n", err);
        return JNI_ERR;
    }

    err = (*env)->SetEventNotificationMode(
            env, JVMTI_ENABLE, JVMTI_EVENT_RESOURCE_EXHAUSTED, NULL);
    if (err != JVMTI_ERROR_NONE) {
        fprintf(stderr, "OOMKiller: SetEventNotificationMode failed, code = %d\n", err);
        return JNI_ERR;
    }

    return JNI_OK;
}
#pragma clang diagnostic pop


#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
static void JNICALL
resourceExhausted(
        jvmtiEnv *jvmti_env,
        JNIEnv *jni_env,
        jint flags,
        const void *reserved,
        const char *description) {

    fprintf(stderr, "OOMKiller: %s: killing current process.\n", description);
    kill(getpid(), SIGKILL);
}
#pragma clang diagnostic pop
