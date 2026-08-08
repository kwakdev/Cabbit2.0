# Deploy on Proxmox: VM or LXC

The recommended production shape is one Ubuntu Server VM with 4 vCPU, 12-16 GB RAM and at least 150 GB on SSD-backed storage. A VM gives Docker its expected kernel boundary, simpler upgrades and clearer vendor support. LXC works for a small trusted installation but requires nesting/keyctl and shares the Proxmox kernel.

Examples assume Proxmox VE 8/9, Ubuntu 24.04, VM ID `220`, LXC ID `221`, bridge `vmbr0`, address `192.168.10.50/24`, gateway `192.168.10.1`, and `/opt/cabbit`. Replace these for your network. Run host commands from the Proxmox shell and guest commands inside the guest.

## Option A - Ubuntu VM (recommended)

Download an Ubuntu cloud image, then create the guest:

```bash
wget -O /var/lib/vz/template/iso/ubuntu-24.04-server-cloudimg-amd64.img https://cloud-images.ubuntu.com/noble/current/noble-server-cloudimg-amd64.img
qm create 220 --name cabbit-prod --ostype l26 --memory 14336 --cores 4 --cpu host --net0 virtio,bridge=vmbr0,firewall=1 --scsihw virtio-scsi-pci --agent enabled=1
qm set 220 --scsi0 local-lvm:0,import-from=/var/lib/vz/template/iso/ubuntu-24.04-server-cloudimg-amd64.img,discard=on,ssd=1
qm resize 220 scsi0 150G
qm set 220 --ide2 local-lvm:cloudinit --boot order=scsi0 --serial0 socket --vga serial0
qm set 220 --ciuser cabbitadmin --sshkeys /root/.ssh/authorized_keys --ipconfig0 ip=192.168.10.50/24,gw=192.168.10.1 --nameserver 1.1.1.1
qm start 220
```

Use storage IDs that exist on your node; `pvesm status` lists them. Put production and staging in separate guests. Do not clone a running database VM without application-consistent backup coordination.

## Option B - Ubuntu LXC

Download an Ubuntu template from the Proxmox UI or with `pveam`, then create an unprivileged container:

```bash
pveam update
pveam available --section system | grep ubuntu-24.04
pveam download local <template-name-from-previous-command>
pct create 221 local:vztmpl/<template-name-from-previous-command> --hostname cabbit-prod --cores 4 --memory 14336 --swap 2048 --rootfs local-lvm:150 --net0 name=eth0,bridge=vmbr0,firewall=1,ip=192.168.10.50/24,gw=192.168.10.1,type=veth --unprivileged 1 --features nesting=1,keyctl=1 --onboot 1
pct start 221
```

Use an LXC only when your backup/restore test includes Docker volumes. Do not use a privileged LXC. If overlay storage fails with permissions, set `fuse=1` and use `fuse-overlayfs`; do not weaken AppArmor globally.

## Base guest hardening

Run in either guest:

```bash
sudo apt update && sudo apt full-upgrade -y
sudo apt install -y ca-certificates curl gnupg git jq fail2ban unattended-upgrades qemu-guest-agent openssl
sudo systemctl enable --now qemu-guest-agent fail2ban unattended-upgrades
sudo timedatectl set-timezone America/Toronto
sudo timedatectl set-ntp true
sudo adduser deploy
sudo usermod -aG sudo deploy
sudo install -d -m 700 -o deploy -g deploy /home/deploy/.ssh
sudo cp ~/.ssh/authorized_keys /home/deploy/.ssh/authorized_keys
sudo chown deploy:deploy /home/deploy/.ssh/authorized_keys
sudo chmod 600 /home/deploy/.ssh/authorized_keys
```

Confirm SSH key access as `deploy` in a second terminal before changing SSH:

```bash
sudo tee /etc/ssh/sshd_config.d/99-cabbit-hardening.conf >/dev/null <<'EOF'
PasswordAuthentication no
PermitRootLogin no
KbdInteractiveAuthentication no
MaxAuthTries 4
EOF
sudo sshd -t && sudo systemctl reload ssh
```

For a VM, enable the guest agent in Proxmox after it reports healthy. For LXC, the host already manages lifecycle and the guest agent is unnecessary.

## Install Docker Engine

```bash
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list >/dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker deploy
sudo systemctl enable --now docker
```

Create `/etc/docker/daemon.json` and restart Docker:

```json
{"log-driver":"local","log-opts":{"max-size":"10m","max-file":"5"},"live-restore":true}
```

```bash
sudo systemctl restart docker
```

Log out and back in so `deploy` receives the Docker group. Treat membership in that group as root-equivalent.

## Network model

Choose exactly one initial access model.

### Private Tailscale (recommended for pilot)

Install Tailscale on the guest, tag the device in your tailnet, and restrict ACLs so field users reach only TCP 443 while administrators may reach SSH and monitoring:

```bash
curl -fsSL https://tailscale.com/install.sh | sh
sudo tailscale up --ssh=false --hostname=cabbit-prod --advertise-tags=tag:field-api
```

In `infra/.env`, leave `COMPOSE_PROFILES=` blank. Start the stack, then let Tailscale terminate trusted HTTPS and proxy to the loopback-only API:

```bash
tailscale serve --bg --https=443 http://127.0.0.1:8080
tailscale serve status
```

Set `EXPO_PUBLIC_API_URL=https://cabbit-prod.YOUR-TAILNET.ts.net/api/v1` for mobile staging/production builds. Test on cellular and external Wi-Fi with Tailscale enabled.

### Public domain

Create an A/AAAA record to the site, forward only TCP 80/443 and UDP 443, set `COMPOSE_PROFILES=public` and `APP_HOSTNAME=api.example.com`, and start Compose. Caddy obtains and renews the certificate. Never forward SQL Server, MinIO or monitoring ports.

## Install and configure Cabbit

```bash
sudo install -d -m 0750 -o deploy -g deploy /opt/cabbit
sudo -u deploy git clone https://github.com/YOUR_ORG/YOUR_REPO.git /opt/cabbit/repository
cd /opt/cabbit/repository
cp .env.example infra/.env
chmod 600 infra/.env
openssl rand -base64 48
openssl rand -base64 32
```

Edit `infra/.env`: replace every `CHANGE_ME`, set hostname/access mode, set a unique bootstrap administrator, and set `APP_VERSION`. Keep the file owned by `deploy`, mode 600, and outside Git. Validate and start:

```bash
cd /opt/cabbit/repository/infra
docker compose --env-file .env config --quiet
docker compose --env-file .env pull
docker compose --env-file .env up -d --build
docker compose --env-file .env ps
./scripts/smoke.sh "https://api.example.com"
```

For Tailscale mode, Caddy is omitted when the profile is blank; use the `.ts.net` smoke-test URL. Sign in with the bootstrap administrator, change its password immediately, create each employee with the least-privileged role, then revoke the bootstrap session.

## Firewall, storage and resource policy

Enable the Proxmox datacenter/node/guest firewall. Permit management SSH only from the admin VLAN/tailnet, permit application HTTPS only from the intended network, and deny inbound traffic by default. Optionally mirror this with UFW; Docker-published ports require DOCKER-USER chain rules, so validate externally rather than trusting `ufw status` alone.

Allocate at least 100 GB to `/var/lib/docker` and 50 GB to encrypted off-host backups. Alert at 80% disk use. A weekly `docker system prune --filter until=168h` may remove old images/build cache, but never prune volumes. Keep 4 vCPU/12-16 GB production, 2-4 vCPU/6-8 GB staging; cap SQL Server memory before increasing other services.

## Backups and Proxmox policy

The RPO is 24 hours and RTO is four hours. Schedule `infra/scripts/backup.sh nightly` after business hours, copy its encrypted output to separate physical storage, and verify with `verify-backup.sh`. Retain seven daily, four weekly and twelve monthly sets off-host. Schedule a nightly Proxmox backup in snapshot mode. Application backups remain necessary because VM snapshots alone are not a database recovery strategy.

Quarterly: restore the SQL `.bak` and MinIO archive into an isolated guest, run migrations, sign in, open a photo, and complete `smoke.sh`. Record date, operator, backup ID, duration and result. Never test restoration over production.

## Updates, rollback and monitoring

Run `scripts/deploy.sh` for a health-checked deployment; it creates a pre-deploy backup, records the old image, starts the new image, and invokes rollback when readiness fails. Database migrations are forward-only; for a destructive migration, restore into a separate database and switch only after validation.

Connect to Uptime Kuma/Grafana through an SSH or Tailscale port forward because they bind to loopback. Configure checks for HTTPS, readiness, storage, CPU/memory, backup age and certificate expiry. Route downtime, disk, backup and deployment failures to an actively monitored channel.

## Proxmox recovery

1. Stop incoming traffic and preserve logs/evidence.
2. Restore the latest verified VM/LXC backup as a new guest on an isolated address.
3. Restore the newest encrypted application backup newer than the guest snapshot if required.
4. Start SQL/MinIO, then API, then proxy; run readiness, authentication, work-order and photo checks.
5. Move DNS/Tailscale identity to the recovered guest, monitor errors/sync backlog, and document actual RPO/RTO.
