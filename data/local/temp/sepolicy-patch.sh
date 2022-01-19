# Clear hash file for sepolicy integrity check --TODO: calculate updated hash and replace 
echo "" > /system/etc/selinux/plat_and_mapping_sepolicy.cil.sha256

# Inject necessary rules
chmod +x /data/local/tmp/sepolicy-inject
/data/local/tmp/sepolicy-inject -s system_app -t sysfs -c file -p write -P /vendor/etc/selinux/precompiled_sepolicy -l
