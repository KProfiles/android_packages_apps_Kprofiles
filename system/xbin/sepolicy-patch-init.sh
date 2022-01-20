#!/system/bin/sh

# Inject necessary rules
touch /data/local/temp/it-worked
chmod +x /system/xbin/sepolicy-inject
/system/xbin/sepolicy-inject -s system_app -t sysfs -c file -p read,write,open,getattr -P /vendor/etc/selinux/precompiled_sepolicy -l
