SET PROBE_MSI_VERSION=11.3.0
SET WIX_PATH=..\tools\wix

%WIX_PATH%\candle.exe -dVERSION=%PROBE_MSI_VERSION% novell-zenworks-probe.wxs
%WIX_PATH%\light.exe -out novell-zenworks-probe.%PROBE_MSI_VERSION%.msi novell-zenworks-probe.wixobj
