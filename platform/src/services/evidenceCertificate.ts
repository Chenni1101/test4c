import { jsPDF } from 'jspdf'
import QRCode from 'qrcode'

export type CertificateData = {
  assetCode: string
  contentSha256: string
  cid?: string | null
  transactionId?: string | null
  blockHeight?: number | string | null
  mode?: string | null
}

const trim = (value?: string | null, length = 56) => value && value.length > length ? `${value.slice(0, length)}…` : (value || 'Not returned')

export async function downloadEvidenceCertificate(data: CertificateData) {
  const verificationUrl = `${window.location.origin}${import.meta.env.BASE_URL}trace?assetCode=${encodeURIComponent(data.assetCode)}`
  const qr = await QRCode.toDataURL(verificationUrl, { width: 240, margin: 1, errorCorrectionLevel: 'M' })
  const doc = new jsPDF({ unit: 'mm', format: 'a4' })
  doc.setFillColor(11, 79, 108); doc.rect(0, 0, 210, 36, 'F')
  doc.setTextColor(255, 255, 255); doc.setFontSize(20); doc.text('DIGITAL ASSET EVIDENCE CERTIFICATE', 18, 20)
  doc.setFontSize(9); doc.text('Cultural Digital Asset Platform - Evidence Summary', 18, 28)
  doc.setTextColor(15, 23, 42); doc.setFontSize(12); doc.text(`Asset code: ${data.assetCode}`, 18, 51)
  doc.setDrawColor(203, 213, 225); doc.line(18, 57, 192, 57)
  const rows: [string, string][] = [
    ['SHA-256', trim(data.contentSha256)], ['Storage CID', trim(data.cid)], ['Transaction ID', trim(data.transactionId)],
    ['Block height', data.blockHeight == null || data.blockHeight === '' ? 'Not returned' : String(data.blockHeight)]
  ]
  let y = 69
  rows.forEach(([label, value]) => { doc.setFontSize(10); doc.setTextColor(71, 85, 105); doc.text(label, 20, y); doc.setTextColor(15, 23, 42); doc.setFont('courier', 'normal'); doc.setFontSize(8); doc.text(value, 62, y); doc.setFont('helvetica', 'normal'); y += 15 })
  doc.addImage(qr, 'PNG', 145, 155, 42, 42)
  doc.setFillColor(255, 247, 237); doc.roundedRect(18, 160, 112, 33, 3, 3, 'F')
  doc.setTextColor(146, 64, 14); doc.setFontSize(9); doc.text('VISUAL PLATFORM MARK - NOT AN ELECTRONIC SIGNATURE', 24, 173)
  doc.setTextColor(71, 85, 105); doc.setFontSize(8); doc.text('This certificate is a system-generated evidence summary.', 24, 182); doc.text('Verify its current status through the QR code or platform trace page.', 24, 188)
  doc.setTextColor(100, 116, 139); doc.setFontSize(7); doc.text(`Generated at ${new Date().toISOString()}`, 18, 278); doc.text('AI-generated content and platform records are not legal copyright determinations.', 18, 284)
  doc.save(`${data.assetCode}-evidence-certificate.pdf`)
}
