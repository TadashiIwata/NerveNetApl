package jp.co.nassua.nervenet.groupchatmain;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UpdateTerminalInfomation {
    public String user;
    public String sipuri;

    private static String updateuser;
    private static String updatesipuri;

    private static GroupCommon groupCommon;

    public void setUpdateInfo(UpdateTerminalInfomation updateInfo) {
        updateuser = updateInfo.user;
        updatesipuri = updateInfo.sipuri;
    }

    public UpdateTerminalInfomation get() {
        UpdateTerminalInfomation Info = new UpdateTerminalInfomation();
        Info.user = updateuser;
        Info.sipuri = updatesipuri;
        return Info;
    }

    public void UpdateXml() {
        // XMLからユーザを削除する。
        if (groupCommon == null) {
            groupCommon = new GroupCommon();
        }

        // 例： <name terminalid="boat-VF750F">VF750F</name>
        String wkGroupname = "<name terminalid=\"" + updatesipuri + "\">" + updateuser  + "</name>";
        int len = wkGroupname.length();
        int len1, len2, idx1, idx2;

        String path = Environment.getExternalStorageDirectory().toString() + "/VoiceMessage/";
        File vmfile = new File(path + groupCommon.VM_Group_Terminal_List);

        StringBuffer stringBuffer = new StringBuffer("");
        try {
            FileReader fileReader = new FileReader(vmfile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String data = null;
            idx1 = -1;
            while ((data = bufferedReader.readLine()) != null) {
                data = data.trim();
                idx1 = data.indexOf(wkGroupname);
                if (idx1 != -1) {
                    String data1 = null;
                    String data2 = null;
                    if (idx1 == 0) {
                        // 先頭
                        len1 = data.length();
                        if (len > len1) {
                            idx1 = len1 + 1;
                            len2 = len - len1;
                            data1 = data.substring(idx1);
                            stringBuffer.append(data1);
                        }
                    } else {
                        // 先頭以外
                        idx2 = idx1 + len;
                        data1 = data.substring(0, idx1);
                        data2 = data.substring(idx2);
                        stringBuffer.append(data1);
                        stringBuffer.append(data2);
                    }
                } else {
                    stringBuffer.append(data);
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String newRecord = stringBuffer.toString();
        try {
            FileWriter fileWriter = new FileWriter(vmfile);
            fileWriter.write(newRecord);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
