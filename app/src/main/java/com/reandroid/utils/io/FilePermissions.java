/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.utils.io;

import java.io.File;

public class FilePermissions {

    private int value;

    public FilePermissions() {
    }

    public boolean apply(File file) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) return false;
        Permission owner = owner();
        Permission group = group();
        Permission others = others();
        boolean result;

        boolean applied = file.setExecutable(owner.execute(),
                owner.execute() && !group.execute() && !others.execute());
        result = applied;

        applied = file.setWritable(owner.write(),
                owner.write() && !group.write() && !others.write());

        result |= applied;
        applied = file.setReadable(owner.read(),
                owner.read() && !group.read() && !others.read());

        result |= applied;

        return result;
    }
    public int get() {
        return value;
    }
    public void set(int value) {
        if((value & 0xffff0000) != 0) {
            throw new NumberFormatException("Value out of range: " + value);
        }
        this.value = value;
    }

    public int high() {
        return (get()) / 512;
    }
    public void high(int high) {
        if(high < 0 || high > 511) {
            throw new NumberFormatException("High value out of range: " + high);
        }
        set (high * 512 + permissions());
    }
    public int permissions() {
        return owner().get() * 64 + group().get() * 8 + others().get();
    }
    public void permissions(int permissions) {
        if(permissions < 0 || permissions > 511) {
            throw new NumberFormatException("Permissions value out of range: " + permissions);
        }
        set(high() * 512 + permissions);
    }
    public Permission owner() {
        return new Permission(this, 2);
    }
    public Permission group() {
        return new Permission(this, 1);
    }
    public Permission others() {
        return new Permission(this, 0);
    }
    public String octal() {
        return '0' + Integer.toOctalString(get());
    }
    public String permissionsOctal() {
        return '0' + Integer.toOctalString(permissions());
    }
    public String permissionsString() {
        return '-' + owner().getString() + group().getString() + others().getString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FilePermissions other = (FilePermissions) obj;
        return this.get() == other.get();
    }
    @Override
    public int hashCode() {
        return get();
    }

    @Override
    public String toString() {
        return octal() + ' ' + permissionsString();
    }
    public static FilePermissions of(int value) {
        FilePermissions filePermissions  = new FilePermissions();
        filePermissions.set(value);
        return filePermissions;
    }

    public static class Permission {

        private final FilePermissions permissions;
        private final int index;

        Permission(FilePermissions permissions, int index) {
            this.permissions = permissions;
            this.index = index;
        }

        public boolean execute() {
            return (get() & 0x1) != 0;
        }
        public void execute(boolean b) {
            int subValue = get();
            if(b) {
                subValue = subValue | 0x1;
            } else {
                subValue = subValue & 0xe;
            }
            set(subValue);
        }
        public boolean write() {
            return (get() & 0x2) != 0;
        }
        public void write(boolean b) {
            int subValue = get();
            if(b) {
                subValue = subValue | 0x2;
            } else {
                subValue = subValue & 0xd;
            }
            set(subValue);
        }
        public boolean read() {
            return (get() & 0x4) != 0;
        }
        public void read(boolean b) {
            int subValue = get();
            if(b) {
                subValue = subValue | 0x4;
            } else {
                subValue = subValue & 0xb;
            }
            set(subValue);
        }

        public int get() {
            return getFor(this.index);
        }
        public void set(int triad) {
            if(triad < 0 || triad > 7) {
                throw new NumberFormatException("Permission triad out of range: " + triad);
            }
            int factor = 1;
            int result = 0;
            int index = this.index;
            for(int i = 0; i < 6; i++) {
                int triadValue;
                if (i == index) {
                    triadValue = triad;
                } else {
                    triadValue = getFor(i);
                }
                result += (triadValue * factor);
                factor = factor * 8;
            }
            permissions.set(result);
        }
        private int getFor(int index) {
            int factor = 1;
            for(int i = 0; i < index; i++) {
                factor = factor * 8;
            }
            return  (permissions.get() % (factor * 8)) / factor;
        }
        public String getString() {
            int triad = get();
            byte[] bytes = new byte[3];
            bytes[0] = (byte) (((triad & 0x4) != 0) ? 'r' : '-');
            bytes[1] = (byte) (((triad & 0x2) != 0) ? 'w' : '-');
            bytes[2] = (byte) (((triad & 0x1) != 0) ? 'x' : '-');
            return new String(bytes);
        }
        public String getName() {
            int index = this.index;
            if(index == 0) {
                return "others";
            }
            if(index == 1) {
                return "group";
            }
            if(index == 2) {
                return "owner";
            }
            return "";
        }
        @Override
        public String toString() {
            return getName() + ' ' + getString();
        }
    }
}
