package bwmirror.generator.c;

import bwmirror.c.CClass;
import bwmirror.c.CDeclaration;
import bwmirror.c.DeclarationType;
import bwmirror.c.Field;
import bwmirror.generator.JavaContext;
import bwmirror.impl.ClassVariable;

import java.io.PrintStream;
import java.util.List;

/**
 * User: PC
 * Date: 18. 6. 2014
 * Time: 11:51
 */
public class Bind {

    private PrintStream out;


    public void setOut(PrintStream out) {
        this.out = out;
    }

    private JavaContext context;

    public Bind(JavaContext context) {
        this.context = context;
    }

    private void implementHelpers() {
        out.println(
                "void flushPrint(const char * text){\n" +
                "   printf(text);\n" +
                "   fflush(stdout); \n" +
                "}\n" +
                "\n" +
                "void println(const char * text){\n" +
                "   printf(text);\n" +
                "   flushPrint(\"\\n\");\n" +
                "}\n"
        );
        out.println();

    }

    private void implementMirror_initTables(List<CDeclaration> declarationList) {
        out.println("JNIEXPORT void JNICALL Java_" + context.getPackageName() + "_Mirror_initTables(JNIEnv * env, jclass jclz){");
        implementVariablesBind(declarationList);
        out.println("}");
        out.println();
    }

    private void implementMirror_getInternalGame() {
        out.println(
                "JNIEXPORT jobject JNICALL Java_" + context.getPackageName() + "_Mirror_getInternalGame(JNIEnv * env, jobject obj){\n" +
                "   jclass gamecls = env->FindClass(\"Lbwapi/Game;\");\n" +
                "   jmethodID getMethodID = env->GetStaticMethodID(gamecls, \"get\", \"(J)Lbwapi/Game;\");\n" +
                "   return env->CallStaticObjectMethod(gamecls, getMethodID, (long)BroodwarPtr);\n" +
                "}\n"
        );
        out.println();
    }

    private void implementMirror_processGameEvents() {
        out.println(
                "JNIEXPORT void JNICALL Java_" + context.getPackageName() + "_Mirror_processGameEvents(JNIEnv * env, jobject obj){\n" +
                "	jclass cls = env->GetObjectClass(obj);\n" +
                "	jobject moduleObj = env->GetObjectField(obj, env->GetFieldID(cls, \"module\", \"Lbwapi/AIModule;\"));\n" +
                "	jclass moduleCls = env->GetObjectClass(moduleObj);\n" +
                "\n" +
                "	jclass unitCls = env->FindClass(\"Lbwapi/Unit;\");\n" +
                "	jclass playerCls = env->FindClass(\"Lbwapi/Player;\");\n" +
                "	jclass posCls = env->FindClass(\"Lbwapi/Position;\");\n" +
                "\n" +
                "	jmethodID matchStartCallback = env->GetMethodID(moduleCls, \"onStart\", \"()V\");\n" +
                "	jmethodID matchEndCallback = env->GetMethodID(moduleCls, \"onEnd\", \"(Z)V\");\n" +
                "	jmethodID matchFrameCallback = env->GetMethodID(moduleCls, \"onFrame\", \"()V\");\n" +
                "	jmethodID sendTextCallback = env->GetMethodID(moduleCls, \"onSendText\", \"(Ljava/lang/String;)V\");\n" +
                "	jmethodID receiveTextCallback = env->GetMethodID(moduleCls, \"onReceiveText\", \"(Lbwapi/Player;Ljava/lang/String;)V\");\n" +
                "	jmethodID playerLeftCallback = env->GetMethodID(moduleCls, \"onPlayerLeft\", \"(Lbwapi/Player;)V\");\n" +
                "	jmethodID nukeDetectCallback = env->GetMethodID(moduleCls, \"onNukeDetect\", \"(Lbwapi/Position;)V\");\n" +
                "	jmethodID unitDiscoverCallback = env->GetMethodID(moduleCls, \"onUnitDiscover\", \"(Lbwapi/Unit;)V\");\n" +
                "	jmethodID unitEvadeCallback = env->GetMethodID(moduleCls, \"onUnitEvade\", \"(Lbwapi/Unit;)V\");\n" +
                "	jmethodID unitShowCallback = env->GetMethodID(moduleCls, \"onUnitShow\", \"(Lbwapi/Unit;)V\");\n" +
                "	jmethodID unitHideCallback = env->GetMethodID(moduleCls, \"onUnitHide\", \"(Lbwapi/Unit;)V\");\n" +
                "	jmethodID unitCreateCallback = env->GetMethodID(moduleCls, \"onUnitCreate\", \"(Lbwapi/Unit;)V\");\n" +
                "	jmethodID unitDestroyCallback = env->GetMethodID(moduleCls, \"onUnitDestroy\", \"(Lbwapi/Unit;)V\");\n" +
                "	jmethodID unitMorphCallback = env->GetMethodID(moduleCls, \"onUnitMorph\", \"(Lbwapi/Unit;)V\");\n" +
                "	jmethodID unitRenegadeCallback = env->GetMethodID(moduleCls, \"onUnitRenegade\", \"(Lbwapi/Unit;)V\");\n" +
                "	jmethodID saveGameCallback = env->GetMethodID(moduleCls, \"onSaveGame\", \"(Ljava/lang/String;)V\");\n" +
                "	jmethodID unitCompleteCallback = env->GetMethodID(moduleCls, \"onUnitComplete\", \"(Lbwapi/Unit;)V\");\n" +
                "\n" +
                "	for (std::list<Event>::const_iterator it = Broodwar->getEvents().begin(); it != Broodwar->getEvents().end(); it++)\n" +
                "	{\n" +
                "		switch (it->getType()) {\n" +
                "		case EventType::MatchStart:\n" +
                "			env->CallVoidMethod(moduleObj, matchStartCallback);\n" +
                "			break;\n" +
                "		case EventType::MatchEnd:\n" +
                "			env->CallVoidMethod(moduleObj, matchEndCallback, it->isWinner());\n" +
                "			break;\n" +
                "		case EventType::MatchFrame:\n" +
                "			env->CallVoidMethod(moduleObj, matchFrameCallback);\n" +
                "			break;\n" +
                "		case EventType::SendText:\n" +
                "			env->CallVoidMethod(moduleObj, sendTextCallback, env->NewStringUTF(it->getText().c_str()));\n" +
                "			break;\n" +
                "		case EventType::ReceiveText:\n" +
                "			env->CallVoidMethod(moduleObj, receiveTextCallback, env->CallStaticObjectMethod(playerCls, env->GetStaticMethodID(playerCls, \"get\", \"(J)Lbwapi/Player;\"), (jlong)it->getPlayer()), env->NewStringUTF(it->getText().c_str()));\n" +
                "			break;\n" +
                "		case EventType::PlayerLeft:\n" +
                "			env->CallVoidMethod(moduleObj, playerLeftCallback, env->CallStaticObjectMethod(playerCls, env->GetStaticMethodID(playerCls, \"get\", \"(J)Lbwapi/Player;\"), (jlong)it->getPlayer()));\n" +
                "			break;\n" +
                "		case EventType::NukeDetect:\n" +
                "			env->CallVoidMethod(moduleObj, nukeDetectCallback, env->NewObject(posCls, env->GetMethodID(posCls, \"<init>\", \"(II)V\"), it->getPosition().x, it->getPosition().y));\n" +
                "			break;\n" +
                "		case EventType::UnitDiscover:\n" +
                "			env->CallVoidMethod(moduleObj, unitDiscoverCallback, env->CallStaticObjectMethod(unitCls, env->GetStaticMethodID(unitCls, \"get\", \"(J)Lbwapi/Unit;\"), (jlong)it->getUnit()));\n" +
                "			break;\n" +
                "		case EventType::UnitEvade:\n" +
                "			env->CallVoidMethod(moduleObj, unitEvadeCallback, env->CallStaticObjectMethod(unitCls, env->GetStaticMethodID(unitCls, \"get\", \"(J)Lbwapi/Unit;\"), (jlong)it->getUnit()));\n" +
                "			break;\n" +
                "		case EventType::UnitShow:\n" +
                "			env->CallVoidMethod(moduleObj, unitShowCallback, env->CallStaticObjectMethod(unitCls, env->GetStaticMethodID(unitCls, \"get\", \"(J)Lbwapi/Unit;\"), (jlong)it->getUnit()));\n" +
                "			break;\n" +
                "		case EventType::UnitHide:\n" +
                "			env->CallVoidMethod(moduleObj, unitHideCallback, env->CallStaticObjectMethod(unitCls, env->GetStaticMethodID(unitCls, \"get\", \"(J)Lbwapi/Unit;\"), (jlong)it->getUnit()));\n" +
                "			break;\n" +
                "		case EventType::UnitCreate:\n" +
                "			env->CallVoidMethod(moduleObj, unitCreateCallback, env->CallStaticObjectMethod(unitCls, env->GetStaticMethodID(unitCls, \"get\", \"(J)Lbwapi/Unit;\"), (jlong)it->getUnit()));\n" +
                "			break;\n" +
                "		case EventType::UnitDestroy:\n" +
                "			env->CallVoidMethod(moduleObj, unitDestroyCallback, env->CallStaticObjectMethod(unitCls, env->GetStaticMethodID(unitCls, \"get\", \"(J)Lbwapi/Unit;\"), (jlong)it->getUnit()));\n" +
                "			break;\n" +
                "		case EventType::UnitMorph:\n" +
                "			env->CallVoidMethod(moduleObj, unitMorphCallback, env->CallStaticObjectMethod(unitCls, env->GetStaticMethodID(unitCls, \"get\", \"(J)Lbwapi/Unit;\"), (jlong)it->getUnit()));\n" +
                "			break;\n" +
                "		case EventType::UnitRenegade:\n" +
                "			env->CallVoidMethod(moduleObj, unitRenegadeCallback, env->CallStaticObjectMethod(unitCls, env->GetStaticMethodID(unitCls, \"get\", \"(J)Lbwapi/Unit;\"), (jlong)it->getUnit()));\n" +
                "			break;\n" +
                "		case EventType::SaveGame:\n" +
                "			env->CallVoidMethod(moduleObj, saveGameCallback, env->NewStringUTF(it->getText().c_str()));\n" +
                "			break;\n" +
                "		case EventType::UnitComplete:\n" +
                "			env->CallVoidMethod(moduleObj, unitCompleteCallback, env->CallStaticObjectMethod(unitCls, env->GetStaticMethodID(unitCls, \"get\", \"(J)Lbwapi/Unit;\"), (jlong)it->getUnit()));\n" +
                "			break;\n" +
                "		}\n" +
                "	}\n" +
                "}\n"
        );
        out.println();
    }

    private void implementMirror_BWAPIClientGetters() {
        out.println(
                "JNIEXPORT jboolean JNICALL Java_bwapi_Mirror_isConnected(JNIEnv * env, jclass jclz){\n" +
                "	return BWAPI::BWAPIClient.isConnected();\n" +
                "}\n" +
                "\n" +
                "JNIEXPORT jboolean JNICALL Java_bwapi_Mirror_connect(JNIEnv * env, jclass jclz){\n" +
                "	return BWAPI::BWAPIClient.connect();\n" +
                "}\n" +
                "\n" +
                "JNIEXPORT void JNICALL Java_bwapi_Mirror_disconnect(JNIEnv * env, jclass jclz){\n" +
                "	BWAPI::BWAPIClient.disconnect();\n" +
                "}\n" +
                "\n" +
                "JNIEXPORT void JNICALL Java_bwapi_Mirror_update(JNIEnv * env, jclass jclz){\n" +
                "	BWAPI::BWAPIClient.update();\n" +
                "}\n"
        );
        out.println();
    }

    private void implementVariablesBind(List<CDeclaration> declarationList) {
        out.println("jclass cls;");
        out.println("jmethodID getId;");
        out.println("jobject cst;");
        for (CDeclaration cDeclaration : declarationList) {
            if (cDeclaration.getDeclType().equals(DeclarationType.CLASS)) {
                bindVariables((CClass) cDeclaration);
            }
        }
    }

    private void bindVariables(CClass cClass) {
        boolean printedIntro = false;
        for (Field field : cClass.getFields()) {
            if (field.getDeclType().equals(DeclarationType.VARIABLE)) {
                if (!printedIntro) {
                    out.println("cls = env->FindClass(\"L" + context.getPackageName() + "/" + cClass.getName() + ";\");");
                    if (cClass.getName().equals("Color")) {
                        out.println("getId = env->GetMethodID(cls,\"<init>\", \"(III)V\");");
                    } else {
                        out.println("getId = env->GetStaticMethodID(cls, \"get\", \"(J)L" + context.getPackageName() + "/" + cClass.getName() + ";\");");
                    }
                    printedIntro = true;
                }
                bindVariable(cClass, (ClassVariable) field);
            }
        }
    }

    private void bindVariable(CClass cClass, ClassVariable classVariable) {

        String cValue = cClass.getName() + "s::" + classVariable.getName();

        if (cClass.getName().equals("Color")) {
            out.println(
                    "env->SetStaticObjectField(cls, " +
                            "env->GetStaticFieldID(cls, \"" + classVariable.getName() + "\", \"L" + context.getPackageName() + "/" + classVariable.getType() + ";\"), " +
                            "env->NewObject(cls, getId, " + cValue + ".red(), " + cValue + ".green(), " + cValue + ".blue())" +
                            ");");
            return;
        }

/*
        out.println("cst = env->GetStaticObjectField(" +
                "cls, " +
                "env->GetStaticFieldID(cls, \"" + classVariable.getName() + "\", \"L" + context.getPackageName() + "/" + classVariable.getType() + ";\")" + ");");
        out.println("env->SetLongField(cst, env->GetFieldID(cls, \"pointer\", \"j\"), (jlong)&" + cValue+");");
                                 */

        out.println(
                      "env->SetStaticObjectField(cls, " +
                              "env->GetStaticFieldID(cls, \"" + classVariable.getName() + "\", \"L" + context.getPackageName() + "/" + classVariable.getType() + ";\"), " +
                              "env->CallStaticObjectMethod(cls, getId, (jlong)&" + cValue + ")" +
                              ");");

        if (cClass.getName().equals("Position") || cClass.getName().equals("TilePosition") || cClass.getName().equals("WalkPosition") || cClass.getName().equals("Point")) {
            return;
        }
        out.println("table" + cClass.getName() + ".insert(std::pair<int, const " + cClass.getName() + "*>(" + cValue + ".getID(), &" + cValue + "));");
    }

    public void implementBind(List<CDeclaration> declarationList) {
        implementHelpers();
        implementMirror_initTables(declarationList);
        implementMirror_getInternalGame();
        implementMirror_processGameEvents();
        implementMirror_BWAPIClientGetters();

    }

}
